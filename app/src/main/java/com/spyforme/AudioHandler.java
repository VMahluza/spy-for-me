package com.spyforme;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class AudioHandler {
    private static final String TAG = "AudioHandler";
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    
    private Context context;
    private AudioRecord audioRecord;
    private HandlerThread recordingThread;
    private Handler recordingHandler;
    private boolean isRecording = false;
    private int bufferSize;

    public AudioHandler(Context context) {
        this.context = context;
        this.bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    }

    public void startRecording() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Audio recording permission not granted");
            return;
        }

        if (isRecording) {
            Log.w(TAG, "Recording already in progress");
            return;
        }

        try {
            // Initialize AudioRecord
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed");
                return;
            }

            // Start recording thread
            recordingThread = new HandlerThread("AudioRecording");
            recordingThread.start();
            recordingHandler = new Handler(recordingThread.getLooper());

            audioRecord.startRecording();
            isRecording = true;

            // Post recording task to handler
            recordingHandler.post(recordingRunnable);

            Log.d(TAG, "Audio recording started");
        } catch (SecurityException | IllegalStateException e) {
            Log.e(TAG, "Error starting audio recording", e);
        }
    }

    private final Runnable recordingRunnable = new Runnable() {
        @Override
        public void run() {
            byte[] buffer = new byte[bufferSize];
            
            while (isRecording && audioRecord != null) {
                int bytesRead = audioRecord.read(buffer, 0, bufferSize);
                
                if (bytesRead > 0) {
                    // Audio data is being captured
                    // Could be processed or saved here
                    processAudioData(buffer, bytesRead);
                } else if (bytesRead < 0) {
                    Log.e(TAG, "Error reading audio data: " + bytesRead);
                    break;
                }
            }
        }
    };

    private void processAudioData(byte[] data, int bytesRead) {
        // Process audio data here
        // For now, just log that data is being captured
        if (bytesRead > 0) {
            // Calculate RMS (Root Mean Square) for audio level
            long sum = 0;
            for (int i = 0; i < bytesRead; i++) {
                sum += data[i] * data[i];
            }
            double rms = Math.sqrt(sum / (double) bytesRead);
            
            // Only log when there's significant audio (reduce log spam)
            if (rms > 10) {
                Log.d(TAG, "Audio level: " + rms);
            }
        }
    }

    public void stopRecording() {
        Log.d(TAG, "Stopping audio recording");
        isRecording = false;

        if (audioRecord != null) {
            try {
                if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    audioRecord.stop();
                }
                audioRecord.release();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error stopping audio recording", e);
            } finally {
                audioRecord = null;
            }
        }

        if (recordingThread != null) {
            recordingThread.quitSafely();
            try {
                recordingThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error joining recording thread", e);
            }
            recordingThread = null;
            recordingHandler = null;
        }

        Log.d(TAG, "Audio recording stopped");
    }

    public boolean isRecording() {
        return isRecording;
    }
}
