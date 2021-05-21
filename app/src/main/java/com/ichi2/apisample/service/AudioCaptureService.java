package com.ichi2.apisample.service;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.ichi2.apisample.helper.PcmToWavUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioCaptureService extends Service {
    public final static String ACTION_START = "AudioCaptureService:Start";
    public final static String ACTION_STOP = "AudioCaptureService:Stop";
    public final static String EXTRA_RESULT_DATA = "AudioCaptureService:Extra:ResultData";

    private final static int SERVICE_ID = 123; // @fixme
    private final static String NOTIFICATION_CHANNEL_ID = "AudioCapture channel";

    private final static int NUM_SAMPLES_PER_READ = 1024;
    private final static int BYTES_PER_SAMPLE = 2;
    private final static int BUFFER_SIZE_IN_BYTES = NUM_SAMPLES_PER_READ * BYTES_PER_SAMPLE;

    private MediaProjectionManager projectionManager;
    private MediaProjection projection;

    private Thread captureThread;
    private AudioRecord record;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(
                SERVICE_ID,
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).build()
        );

        projectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void createNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Audio Capture Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    @RequiresApi(value = 29)
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return Service.START_NOT_STICKY;
        }
        switch (intent.getAction()) {
            case ACTION_START:
                projection = projectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) intent.getParcelableExtra(EXTRA_RESULT_DATA));
                startAudioCapture();
                return Service.START_STICKY;
            case ACTION_STOP:
                stopAudioCapture();
                return Service.START_NOT_STICKY;
            default:
                throw new IllegalArgumentException();
        }
    }

    @RequiresApi(value = 29)
    private void startAudioCapture() {
        AudioPlaybackCaptureConfiguration config = new AudioPlaybackCaptureConfiguration.Builder(projection)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .build();

        AudioFormat format = new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                .build();

        record = new AudioRecord.Builder()
                .setAudioFormat(format)
                .setBufferSizeInBytes(BUFFER_SIZE_IN_BYTES)
                .setAudioPlaybackCaptureConfig(config)
                .build();

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
                record.read(capturedSamples, 0, NUM_SAMPLES_PER_READ);
                fileOutputStream.write(toByteArray(capturedSamples), 0, BUFFER_SIZE_IN_BYTES);
            }

            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error();
        }
    }



    private void stopAudioCapture() {
        if (projection == null) {
            throw new IllegalStateException();
        }

        captureThread.interrupt();
        try {
            captureThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new Error();
        }

        try {
            PcmToWavUtil converter = new PcmToWavUtil(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            String pathname = file.getAbsolutePath();
            String convertedPathname = pathname.substring(0, pathname.lastIndexOf(".")) + ".wav";
            File wavFile = new File(convertedPathname);
            wavFile.createNewFile();
            converter.pcmToWav(pathname, convertedPathname);
            file.delete();
        } catch (IOException e) {
            throw new Error();
        }

        record.stop();
        record.release();
        record = null;

        projection.stop();
        stopSelf();
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
