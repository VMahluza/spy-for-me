package com.spyforme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private Button startServiceButton;
    private Button stopServiceButton;
    
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.POST_NOTIFICATIONS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startServiceButton = findViewById(R.id.startServiceButton);
        stopServiceButton = findViewById(R.id.stopServiceButton);

        startServiceButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                startSpyService();
            } else {
                requestPermissions();
            }
        });

        stopServiceButton.setOnClickListener(v -> stopSpyService());
    }

    private boolean checkPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        
        for (String permission : REQUIRED_PERMISSIONS) {
            // Skip POST_NOTIFICATIONS check for Android versions below 13
            if (permission.equals(Manifest.permission.POST_NOTIFICATIONS) 
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                continue;
            }
            
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        
        return missingPermissions.isEmpty();
    }

    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        for (String permission : REQUIRED_PERMISSIONS) {
            // Skip POST_NOTIFICATIONS request for Android versions below 13
            if (permission.equals(Manifest.permission.POST_NOTIFICATIONS) 
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                continue;
            }
            
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();
                startSpyService();
            } else {
                Toast.makeText(this, "Some permissions denied. App may not work correctly.", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startSpyService() {
        Intent serviceIntent = new Intent(this, BackgroundSpyService.class);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        Toast.makeText(this, "Spy Service Started", Toast.LENGTH_SHORT).show();
    }

    private void stopSpyService() {
        Intent serviceIntent = new Intent(this, BackgroundSpyService.class);
        stopService(serviceIntent);
        Toast.makeText(this, "Spy Service Stopped", Toast.LENGTH_SHORT).show();
    }
}
