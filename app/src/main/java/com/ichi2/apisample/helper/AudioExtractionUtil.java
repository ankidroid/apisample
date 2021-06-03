package com.ichi2.apisample.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioExtractionUtil {
    private static final int BUFFER_CAPACITY = 500 * 1024;

    public static Uri extract(Context context, Uri sourceUri, String destFilePath) {
        ContentResolver resolver = context.getContentResolver();
        String type = resolver.getType(sourceUri);
        if (!type.equals("video/mp4")) {
            return null;
        }
        try {
            MediaMuxer mediaMuxer = null;
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(context, sourceUri, null);
            int trackCount = mediaExtractor.getTrackCount();
            int audioTrackIndex = -1;
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                String mime = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    mediaExtractor.selectTrack(i);
                    mediaMuxer = new MediaMuxer(destFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    audioTrackIndex = mediaMuxer.addTrack(trackFormat);
                    mediaMuxer.start();
                    break;
                }
            }

            if (mediaMuxer == null) {
                return null;
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);

            int i;
            while ((i = mediaExtractor.readSampleData(byteBuffer, 0)) > 0) {
                bufferInfo.offset = 0;
                bufferInfo.size = i;
                bufferInfo.flags = mediaExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(audioTrackIndex, byteBuffer, bufferInfo);
                mediaExtractor.advance();
            }

            mediaExtractor.release();
            mediaMuxer.stop();
            mediaMuxer.release();

            File dstFile = new File(destFilePath);
            return UriUtil.getUriForFile(context, dstFile);
        } catch (IOException e) {
            return null;
        }
    }
}
