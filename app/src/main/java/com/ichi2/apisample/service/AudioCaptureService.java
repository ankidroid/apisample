package com.ichi2.apisample.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ichi2.apisample.R;
import com.ichi2.apisample.helper.PcmToWavUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AudioCaptureService extends Service {
    public final static String EXTRA_RESULT_DATA = "AudioCaptureService:Extra:ResultData";

    public final static String ACTION_FILE_CREATED = "AudioCaptureService:FileCreated";
    public final static String EXTRA_PATHNAME = "AudioCaptureService:Extra:Pathname";

    private final static int SERVICE_ID = 1;
    private final static String NOTIFICATION_CHANNEL_ID = "AudioCapture channel";
    private final static String NOTIFICATION_CHANNEL_NAME = "Audio Capture Service Channel";

    private final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final int SAMPLE_RATE = 44100;
    private final int CHANNEL_MASK = AudioFormat.CHANNEL_IN_STEREO;

    private final static int NUM_SAMPLES_PER_READ = 1024;
    private final static int BYTES_PER_SAMPLE = 2;
    private final static int BUFFER_SIZE_IN_BYTES = NUM_SAMPLES_PER_READ * BYTES_PER_SAMPLE;

    private Thread captureThread;
    private MediaProjection projection;
    private AudioRecord record;

    private Handler handler;

    private WindowManager windowManager;
    private View overlayView;
    private TextView textTop;
    private TextView textBottom;

    private long recordingStartedAt;
    private int recordedFilesCount;

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public void onCreate() {
        super.onCreate();
        NotificationChannel notificationChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
        startForeground(
                SERVICE_ID,
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).build()
        );

        handler = new Handler();

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        layoutParams.setTitle("overlayy"); // @fixme
        layoutParams.x = 0;
        layoutParams.y = 0;

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_recording, null, false);

        final Button actionStart = overlayView.findViewById(R.id.actionStart);
        final Button actionStop = overlayView.findViewById(R.id.actionStop);

        actionStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionStart.setEnabled(false);
                startAudioCapture();
                actionStop.setEnabled(true);
            }
        });

        actionStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionStop.setEnabled(false);
                stopAudioCapture();
                actionStart.setEnabled(true);
            }
        });
        actionStop.setEnabled(false);

        Button actionClose = overlayView.findViewById(R.id.actionClose);
        actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (captureThread != null && captureThread.isAlive()) {
                    captureThread.interrupt();
                    record.stop();
                    file.delete();
                }
                record.release();
                projection.stop();
                stopSelf();
            }
        });

        textTop = overlayView.findViewById(R.id.textTop);
        textBottom = overlayView.findViewById(R.id.textBottom);

        windowManager.addView(overlayView, layoutParams);
    }

    @Override
    public void onDestroy() {
        windowManager.removeView(overlayView);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.Q)
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return Service.START_NOT_STICKY;
        }
        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        projection = projectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) intent.getParcelableExtra(EXTRA_RESULT_DATA));
        AudioPlaybackCaptureConfiguration config = new AudioPlaybackCaptureConfiguration.Builder(projection)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        AudioFormat format = new AudioFormat.Builder()
                .setEncoding(ENCODING)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(CHANNEL_MASK)
                .build();
        record = new AudioRecord.Builder()
                .setAudioFormat(format)
                .setBufferSizeInBytes(BUFFER_SIZE_IN_BYTES)
                .setAudioPlaybackCaptureConfig(config)
                .build();
        return Service.START_STICKY;
    }

    private void startAudioCapture() {
        recordingStartedAt = System.currentTimeMillis();
        record.startRecording();
        captureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                File outputFile = createAudioFile();
                setFile(outputFile);
                writeAudioToFile(outputFile);
            }
        });
        captureThread.start();
    }

    private File file;

    private void setFile(File file) {
        this.file = file;
    }

    private File createAudioFile() {
        File capturesDir = new File(Environment.getExternalStorageDirectory().getPath() + "/AudioCaptures");
        if (!capturesDir.exists()) {
            capturesDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.US).format(new Date());
        String filename = String.format("Capture-%s.pcm", timestamp);
        return new File(capturesDir.getAbsolutePath() + "/" + filename);
    }

    private void writeAudioToFile(File file) {
        try {
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            short[] capturedSamples = new short[NUM_SAMPLES_PER_READ];

            while (!captureThread.isInterrupted()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshTime(System.currentTimeMillis() - recordingStartedAt);

                    }
                });
                record.read(capturedSamples, 0, NUM_SAMPLES_PER_READ);
                fileOutputStream.write(toByteArray(capturedSamples), 0, BUFFER_SIZE_IN_BYTES);
            }

            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error();
        }
    }

    private void refreshTime(long millis) {
        textTop.setText(
                String.format(
                        Locale.US,
                        "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
                        (TimeUnit.MILLISECONDS.toMillis(millis) % TimeUnit.SECONDS.toMillis(1)) / 10
                )
        );
    }

    private void stopAudioCapture() {
        captureThread.interrupt();
        try {
            captureThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new Error();
        }

        try {
            PcmToWavUtil converter = new PcmToWavUtil(SAMPLE_RATE, CHANNEL_MASK, ENCODING);
            String pathname = file.getAbsolutePath();
            String convertedPathname = pathname.substring(0, pathname.lastIndexOf(".")) + ".wav";
            File wavFile = new File(convertedPathname);
            wavFile.createNewFile();
            converter.pcmToWav(pathname, convertedPathname);
            file.delete();

            Intent intent = new Intent(ACTION_FILE_CREATED);
            intent.putExtra(EXTRA_PATHNAME, convertedPathname);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            recordedFilesCount++;
            textBottom.setText("files recorded: " + recordedFilesCount); // @fixme
            handler.post(new Runnable() {
                @Override
                public void run() {
                    refreshTime(0);
                }
            });
        } catch (IOException e) {
            throw new Error();
        }

        record.stop();
    }

    private static byte[] toByteArray(short[] array) {
        // Samples get translated into bytes following little-endianness:
        // least significant byte first and the most significant byte last
        byte[] result = new byte[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            result[i * 2] = (byte) (array[i] & 0x00FF);
            result[i * 2 + 1] = (byte) ((int) array[i] >> 8);
            array[i] = 0;
        }
        return result;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
