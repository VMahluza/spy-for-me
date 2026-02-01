# Spy For Me - Android Background Monitoring App

An Android application that runs a background service with access to the device's camera and microphone.

## Features

- **Background Service**: Runs as a foreground service to ensure continuous operation
- **Camera Access**: Uses Camera2 API for background camera monitoring
- **Microphone Access**: Captures audio data in the background
- **Permission Handling**: Properly requests and manages runtime permissions for Android 6.0+
- **Notification Support**: Displays a persistent notification while the service is running

## Technical Details

### Permissions Required
- `CAMERA` - Access to device camera
- `RECORD_AUDIO` - Access to device microphone
- `FOREGROUND_SERVICE` - Run as a foreground service
- `FOREGROUND_SERVICE_CAMERA` - Camera access in foreground service (Android 14+)
- `FOREGROUND_SERVICE_MICROPHONE` - Microphone access in foreground service (Android 14+)
- `WAKE_LOCK` - Keep device awake
- `POST_NOTIFICATIONS` - Display notifications (Android 13+)

### Architecture

- **MainActivity**: Main entry point that handles permission requests and service control
- **BackgroundSpyService**: Foreground service that coordinates monitoring activities
- **CameraHandler**: Manages Camera2 API for background camera access
- **AudioHandler**: Manages AudioRecord API for microphone access

### Building the Project

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run on a physical device (emulator may have limitations with camera/microphone)

### Minimum Requirements

- Android SDK 21 (Android 5.0 Lollipop) or higher
- Target SDK 33 (Android 13)

## Usage

1. Launch the app
2. Grant camera and microphone permissions when prompted
3. Tap "Start Monitoring Service" to begin background monitoring
4. The service will run in the background with a persistent notification
5. Tap "Stop Monitoring Service" to stop the service

## Note

This application is intended for educational purposes only. Always ensure you have proper authorization before using any monitoring capabilities on a device.