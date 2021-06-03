package com.ichi2.apisample.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AudioCaptureService extends Service {
    public final static String CAPTURES_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/MusicIntervals2Anki/AudioCaptures";

    public final static String EXTRA_RESULT_DATA = "AudioCaptureService:Extra:ResultData";

    public final static String ACTION_FILE_CREATED = "AudioCaptureService:FileCreated";
    public final static String EXTRA_URI_STRING = "AudioCaptureService:Extra:UriString";

    private final static int SERVICE_ID = 1;
    private final static String NOTIFICATION_CHANNEL_ID = "AudioCapture channel";
    private final static String NOTIFICATION_CHANNEL_NAME = "Audio Capture Service Channel";

    private final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final int SAMPLE_RATE = 44100;
    private final int CHANNEL_MASK = AudioFormat.CHANNEL_IN_STEREO;

    private final static int NUM_SAMPLES_PER_READ = 1024;
    private final static int BYTES_PER_SAMPLE = 2;
    private final static int BUFFER_SIZE_IN_BYTES = NUM_SAMPLES_PER_READ * BYTES_PER_SAMPLE;

    private MediaProjection projection;
    private AudioRecord record;

    private Thread captureThread;
    private Handler handler;

    private File tempPcmFile;

    private boolean isRecording;
    private long recordingStartedAt;
    private int recordedFilesCount;

    private WindowManager windowManager;

    private View overlayView;
    private TextView textTop;
    private Button actionRecord;
    private TextView textBottom;

    private View countdownView;
    private TextView textCount;
    private ArrayList<Runnable> countdownCallbacks;

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
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.x = 0;
        layoutParams.y = 0;

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_recording, null, false);
        View.OnTouchListener moveOnTouchListener = new MoveViewOnTouchListener(windowManager, overlayView);
        overlayView.setOnTouchListener(moveOnTouchListener);

        countdownView = LayoutInflater.from(this).inflate(R.layout.overlay_countdown, null, false);
        countdownView.setVisibility(View.GONE);

        textCount = countdownView.findViewById(R.id.textCount);

        actionRecord = overlayView.findViewById(R.id.actionRecord);
        actionRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) {
                    actionRecord.setEnabled(false);
                    countdownView.setVisibility(View.VISIBLE);

                    countdownCallbacks = new ArrayList<>(4);
                    final int t = 3;
                    for (int i = 0; i < t; i++) {
                        final int count = t - i;
                        Runnable callback = new Runnable() {
                            @Override
                            public void run() {
                                textCount.setText(String.valueOf(count));
                            }
                        };
                        handler.postDelayed(callback, i * 1000);
                        countdownCallbacks.add(callback);
                    }

                    Runnable callback = new Runnable() {
                        @Override
                        public void run() {
                            handleStartCapture();
                        }
                    };
                    handler.postDelayed(callback, (t) * 1000);
                    countdownCallbacks.add(callback);

                } else {
                    stopAudioCapture();
                    isRecording = false;
                    actionRecord.setText(R.string.record);
                }
            }
        });

        Button actionClose = overlayView.findViewById(R.id.actionClose);
        actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tearDown();
            }
        });

        textTop = overlayView.findViewById(R.id.textTop);
        refreshTime(0);
        textBottom = overlayView.findViewById(R.id.textBottom);
        textBottom.setText(getString(R.string.recorded_files, 0));

        Button actionSkip = countdownView.findViewById(R.id.actionSkip);
        actionSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (countdownCallbacks != null) {
                            for (Runnable callback : countdownCallbacks) {
                                handler.removeCallbacks(callback);
                            }
                        }
                        handleStartCapture();
                    }
                });
            }
        });

        windowManager.addView(overlayView, layoutParams);
        windowManager.addView(countdownView, layoutParams);
    }

    private void handleStartCapture() {
        countdownCallbacks = null;
        countdownView.setVisibility(View.GONE);
        startAudioCapture();
        isRecording = true;
        actionRecord.setEnabled(true);
        actionRecord.setText(R.string.stop);
    }

    private void tearDown() {
        if (countdownCallbacks != null) {
            for (Runnable callback : countdownCallbacks) {
                handler.removeCallbacks(callback);
            }
        }
        countdownCallbacks = null;
        if (captureThread != null && captureThread.isAlive()) {
            captureThread.interrupt();
            record.stop();
            tempPcmFile.delete();
        }
        record.release();
        projection.stop();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        windowManager.removeView(overlayView);
        windowManager.removeView(countdownView);
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
                tempPcmFile = createAudioFile();
                writeAudioToFile(tempPcmFile);
            }
        });
        captureThread.start();
    }

    private File createAudioFile() {
        File capturesDir = new File(CAPTURES_DIRECTORY);
        if (!capturesDir.exists()) {
            capturesDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SSS", Locale.US).format(new Date());
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
            String pathname = tempPcmFile.getAbsolutePath();
            String convertedPathname = pathname.substring(0, pathname.lastIndexOf(".")) + ".wav";
            File wavFile = new File(convertedPathname);
            wavFile.createNewFile();
            converter.convert(pathname, convertedPathname);
            tempPcmFile.delete();

            Uri uri = Uri.fromFile(wavFile);

            Intent intent = new Intent(ACTION_FILE_CREATED);
            intent.putExtra(EXTRA_URI_STRING, uri.toString());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            SharedPreferences uiDb = getSharedPreferences(MainActivity.REF_DB_STATE, Context.MODE_PRIVATE);
            boolean afterSelecting = uiDb.getBoolean(MainActivity.REF_DB_AFTER_SELECTING, false);
            boolean afterAdding = uiDb.getBoolean(MainActivity.REF_DB_AFTER_ADDING, false);
            ArrayList<String> newFilenames;
            if (afterSelecting || afterAdding) {
                newFilenames = new ArrayList<>();
                SharedPreferences.Editor uiDbEditor = uiDb.edit();
                uiDbEditor.putBoolean(MainActivity.REF_DB_MISMATCHING_SORTING, false);
                uiDbEditor.putBoolean(MainActivity.REF_DB_SORT_BY_NAME, false);
                uiDbEditor.putBoolean(MainActivity.REF_DB_SORT_BY_DATE, false);
                uiDbEditor.putBoolean(MainActivity.REF_DB_AFTER_SELECTING, false);
                uiDbEditor.putBoolean(MainActivity.REF_DB_AFTER_ADDING, false);
                uiDbEditor.apply();
            } else {
                String[] filenames = MainActivity.getStoredFilenames(this);
                newFilenames = new ArrayList<>(Arrays.asList(filenames));
            }
            newFilenames.add(uri.toString());
            MainActivity.storeFilenames(this, newFilenames.toArray(new String[0]));

            recordedFilesCount++;
            textBottom.setText(getString(R.string.recorded_files, recordedFilesCount));
            handler.post(new Runnable() {
                @Override
                public void run() {
                    refreshTime(0);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
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
