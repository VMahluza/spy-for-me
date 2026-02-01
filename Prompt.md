# Native Android Security Camera App - Complete Implementation Guide

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Project Structure](#project-structure)
3. [Configuration](#configuration)
4. [Core Components](#core-components)
5. [Signaling Server](#signaling-server)
6. [Deployment Instructions](#deployment-instructions)

---

## 🎯 Project Overview

Build a **Native Android Security Camera application** using **Kotlin** that enables peer-to-peer video streaming between two Android devices.

### Architecture
- **Server App**: Runs on the home phone to capture and stream video/audio
- **Client App**: Runs on a remote phone to view the live stream
- **Technology Stack**:
  - WebRTC for peer-to-peer real-time communication
  - Socket.io for WebRTC signaling
  - Android Camera2 API for video capture
  - Foreground Services for background streaming

---

## 📁 Project Structure

Create the following directory structure for the Android application:

```
SecurityCameraApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/securitycam/
│   │   │   ├── MainActivity.kt                    # Main launcher activity
│   │   │   ├── ServerActivity.kt                  # Server-side streaming
│   │   │   ├── ClientActivity.kt                  # Client-side viewing
│   │   │   │
│   │   │   ├── services/
│   │   │   │   └── StreamingService.kt            # Foreground service for background streaming
│   │   │   │
│   │   │   ├── webrtc/
│   │   │   │   ├── WebRTCClient.kt                # WebRTC manager
│   │   │   │   ├── SignalingClient.kt             # Socket.io signaling client
│   │   │   │   └── RTCPeerConnectionObserver.kt   # WebRTC connection callbacks
│   │   │   │
│   │   │   └── utils/
│   │   │       └── Constants.kt                   # App constants
│   │   │
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml              # Main screen layout
│   │   │   │   ├── activity_server.xml            # Server screen layout
│   │   │   │   └── activity_client.xml            # Client screen layout
│   │   │   │
│   │   │   └── values/
│   │   │       ├── strings.xml                    # String resources
│   │   │       └── colors.xml                     # Color resources
│   │   │
│   │   └── AndroidManifest.xml                    # App manifest
│   │
│   └── build.gradle.kts                           # App-level build config
│
├── gradle/                                        # Gradle wrapper files
└── build.gradle.kts                               # Project-level build config
```

---

## ⚙️ Configuration

### 1. App-Level build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.securitycam"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.securitycam"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // WebRTC - Core dependency for peer-to-peer streaming
    implementation("org.webrtc:google-webrtc:1.0.32006")
    
    // Networking - HTTP client and WebSocket support
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.socket:socket.io-client:2.1.0")
    
    // Coroutines - Async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // ViewModel and LiveData - Android Architecture Components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // JSON Parsing
    implementation("com.google.code.gson:gson:2.10.1")
}
```

### 2. AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <uses-feature android:name="android.hardware.camera" android:required="true" />
    <uses-feature android:name="android.hardware.camera.autofocus" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MaterialComponents.DayNight.DarkActionBar"
        android:usesCleartextTraffic="true"
        tools:targetApi="31">
        
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <activity 
            android:name=".ServerActivity"
            android:screenOrientation="portrait" />
        
        <activity 
            android:name=".ClientActivity"
            android:screenOrientation="portrait" />
        
        <service
            android:name=".services.StreamingService"
            android:foregroundServiceType="camera|microphone"
            android:exported="false" />
            
    </application>

</manifest>
```

<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Required Permissions -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <!-- Hardware Requirements -->
    <uses-feature android:name="android.hardware.camera" android:required="true" />
    <uses-feature android:name="android.hardware.camera.autofocus" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MaterialComponents.DayNight.DarkActionBar"
        android:usesCleartextTraffic="true"
        tools:targetApi="31">
        
        <!-- Main Launcher Activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- Server Activity (Camera Stream) -->
        <activity 
            android:name=".ServerActivity"
            android:screenOrientation="portrait"
            android:exported="false" />
        
        <!-- Client Activity (Stream Viewer) -->
        <activity 
            android:name=".ClientActivity"
            android:screenOrientation="portrait"
            android:exported="false" />
        
        <!-- Foreground Streaming Service -->
        <service
            android:name=".services.StreamingService"
            android:foregroundServiceType="camera|microphone"
            android:exported="false" />
            
    </application>

</manifest>
```

### 3. Constants.kt

```kotlin
package com.securitycam.utils

object Constants {
    // ⚠️ IMPORTANT: Replace with your actual signaling server URL
    // Example: "http://192.168.1.100:3000" or "https://your-server.com"
    const val SIGNALING_SERVER_URL = "http://YOUR_SERVER_IP:3000"
    
    // STUN servers for NAT traversal (Google's public STUN servers)
    val ICE_SERVERS = listOf(
        "stun:stun.l.google.com:19302",
        "stun:stun1.l.google.com:19302"
    )
    
    // WebRTC Track Identifiers
    const val LOCAL_TRACK_ID = "local_track"
    const val LOCAL_STREAM_ID = "local_stream"
}
```

---

## 🔧 Core Components

### 1. MainActivity.kt

**Purpose**: Main launcher screen with role selection

**Requirements**:
- Display two Material Design buttons:
  - **"Start as Server (Home Phone)"** → Navigate to `ServerActivity`
  - **"Connect as Client (Remote Phone)"** → Navigate to `ClientActivity`
- Request runtime permissions using `ActivityResultContracts`:
  - `CAMERA`
  - `RECORD_AUDIO`
  - `POST_NOTIFICATIONS` (for Android 13+)
- Modern Material Design 3 UI with proper spacing and styling
- Handle permission denial gracefully with user-friendly messages

**Key Implementation Points**:
```kotlin
// Use ActivityResultLauncher for permissions
private val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    // Handle permission results
}

// Request permissions before launching activities
private fun checkAndRequestPermissions() {
    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.POST_NOTIFICATIONS
    )
    permissionLauncher.launch(permissions)
}
```

---

### 2. ServerActivity.kt

**Purpose**: Stream video/audio from device camera

**Requirements**:

1. **UI Components**:
   - `SurfaceViewRenderer` for local camera preview (full screen)
   - Large, bold `TextView` displaying 6-digit room code
   - Status `TextView` (e.g., "Waiting for client...", "Client Connected")
   - "Stop Streaming" button at bottom

2. **Functionality**:
   - Generate random 6-digit room code on activity start
   - Initialize WebRTC with rear camera and microphone
   - Connect to signaling server and create room
   - Display local camera preview immediately
   - Wait for client to join room
   - Handle WebRTC offer/answer exchange
   - Start `StreamingService` as foreground service
   - Update UI when client connects
   - Properly cleanup WebRTC resources on stop

3. **WebRTC Flow**:
   ```kotlin
   // Initialization
   1. Initialize PeerConnectionFactory
   2. Create local video/audio tracks
   3. Render local preview
   4. Create room on signaling server
   
   // When client joins
   5. Create WebRTC offer
   6. Send offer via signaling server
   7. Wait for answer
   8. Exchange ICE candidates
   9. Start foreground service
   ```

4. **Important Implementation Details**:
   - Use Camera2Enumerator for camera device selection
   - Configure video encoding parameters (resolution, frame rate)
   - Handle camera focus and exposure
   - Enable speaker mode for audio
   - Keep screen on during streaming (`WAKE_LOCK`)

---

### 3. ClientActivity.kt

**Purpose**: View remote video stream

**Requirements**:

1. **UI Components**:
   - `EditText` for entering 6-digit room code (initially visible)
   - "Connect" button
   - `SurfaceViewRenderer` for remote stream (full screen, initially hidden)
   - Connection status `TextView` with states:
     - "Disconnected"
     - "Connecting..."
     - "Connected"
     - "Connection Failed"
   - "Disconnect" button (initially hidden, shown when connected)

2. **Functionality**:
   - Validate room code format (6 digits)
   - Connect to signaling server and join room
   - Handle WebRTC offer/answer exchange
   - Render remote video stream
   - Play remote audio through speaker
   - Update connection status in real-time
   - Handle reconnection on network issues
   - Properly cleanup on disconnect

3. **WebRTC Flow**:
   ```kotlin
   // Connection
   1. Initialize PeerConnectionFactory
   2. Join room on signaling server
   3. Wait for offer from server
   4. Create answer
   5. Send answer via signaling server
   6. Exchange ICE candidates
   7. Render remote stream when received
   ```

4. **UI State Management**:
   ```kotlin
   // Disconnected State
   - Show: EditText, Connect button
   - Hide: SurfaceViewRenderer, Disconnect button
   
   // Connecting State
   - Show: "Connecting..." status
   - Disable: Connect button
   
   // Connected State
   - Show: SurfaceViewRenderer (full screen), Disconnect button
   - Hide: EditText, Connect button
   ```

---

### 4. WebRTCClient.kt

**Purpose**: Manage WebRTC peer connections

**Requirements**:

Create a comprehensive WebRTC manager class with the following capabilities:

**Initialization**:
```kotlin
class WebRTCClient(
    private val context: Context,
    private val observer: RTCPeerConnectionObserver
) {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    
    fun initializePeerConnectionFactory() {
        // Initialize WebRTC library
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        
        // Create PeerConnectionFactory
        // Configure audio/video encoders and decoders
    }
}
```

**Methods to Implement**:

1. `initializePeerConnection()`: Create peer connection with ICE servers
2. `createLocalVideoTrack()`: Initialize camera video track
3. `createLocalAudioTrack()`: Initialize microphone audio track
4. `addStreamToLocalPeer()`: Add local tracks to peer connection
5. `createOffer()`: Create WebRTC offer for server
6. `createAnswer()`: Create WebRTC answer for client
7. `setRemoteDescription()`: Set remote SDP
8. `addIceCandidate()`: Add received ICE candidate
9. `switchCamera()`: Toggle front/rear camera
10. `enableVideo()`: Enable/disable video track
11. `enableAudio()`: Enable/disable audio track
12. `close()`: Cleanup all resources

**Key Implementation Points**:
- Use proper threading (WebRTC requires specific threads for operations)
- Configure video constraints (resolution: 1280x720, fps: 30)
- Configure audio constraints (echo cancellation, noise suppression)
- Handle ICE server configuration
- Properly dispose resources to prevent memory leaks

---

### 5. SignalingClient.kt

**Purpose**: Socket.io client for WebRTC signaling

**Requirements**:

**Socket.io Events to Emit**:
- `create-room` - Server creates a new room
- `join-room` - Client joins existing room
- `offer` - Send WebRTC offer
- `answer` - Send WebRTC answer
- `ice-candidate` - Send ICE candidate

**Socket.io Events to Listen**:
- `room-created` - Room successfully created
- `room-joined` - Successfully joined room
- `client-joined` - Client connected to your room
- `offer` - Received WebRTC offer
- `answer` - Received WebRTC answer
- `ice-candidate` - Received ICE candidate
- `error` - Error occurred

**Implementation Structure**:
```kotlin
class SignalingClient(private val serverUrl: String) {
    private var socket: Socket? = null
    var listener: SignalingListener? = null
    
    interface SignalingListener {
        fun onRoomCreated(roomId: String)
        fun onRoomJoined(roomId: String)
        fun onClientJoined(clientId: String)
        fun onOfferReceived(offer: SessionDescription)
        fun onAnswerReceived(answer: SessionDescription)
        fun onIceCandidateReceived(candidate: IceCandidate)
        fun onError(error: String)
    }
    
    fun connect() { /* Connect to Socket.io server */ }
    fun disconnect() { /* Disconnect from server */ }
    fun createRoom(roomId: String) { /* Create room */ }
    fun joinRoom(roomId: String) { /* Join room */ }
    fun sendOffer(target: String, offer: SessionDescription) { /* Send offer */ }
    fun sendAnswer(target: String, answer: SessionDescription) { /* Send answer */ }
    fun sendIceCandidate(target: String, candidate: IceCandidate) { /* Send ICE */ }
}
```

**Key Implementation Points**:
- Use Kotlin coroutines for async socket operations
- Convert WebRTC objects to JSON for transmission
- Handle connection failures and reconnection logic
- Implement proper error handling
- Log all signaling events for debugging

---

### 6. RTCPeerConnectionObserver.kt

**Purpose**: Implement WebRTC callbacks

**Requirements**:

Implement `PeerConnection.Observer` interface with all required methods:

```kotlin
class RTCPeerConnectionObserver(
    private val onIceCandidateCallback: (IceCandidate) -> Unit,
    private val onAddStreamCallback: (MediaStream) -> Unit,
    private val onConnectionChangeCallback: (PeerConnection.IceConnectionState) -> Unit
) : PeerConnection.Observer {
    
    override fun onIceCandidate(candidate: IceCandidate?) {
        candidate?.let { onIceCandidateCallback(it) }
    }
    
    override fun onAddStream(stream: MediaStream?) {
        stream?.let { onAddStreamCallback(it) }
    }
    
    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
        state?.let { onConnectionChangeCallback(it) }
    }
    
    // Override all other required methods with appropriate logging
    override fun onSignalingChange(state: PeerConnection.SignalingState?) { }
    override fun onIceConnectionReceivingChange(receiving: Boolean) { }
    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) { }
    override fun onRemoveStream(stream: MediaStream?) { }
    override fun onDataChannel(channel: DataChannel?) { }
    override fun onRenegotiationNeeded() { }
}
```

**Key Callbacks**:
1. **onIceCandidate**: Send ICE candidate to remote peer via signaling
2. **onAddStream**: Render remote stream to SurfaceViewRenderer
3. **onIceConnectionChange**: Update UI connection status
4. **onRenegotiationNeeded**: Handle reconnection scenarios

---

### 7. StreamingService.kt

**Purpose**: Foreground service for background streaming

**Requirements**:

1. **Notification Channel**:
   - Create notification channel for Android O+
   - Channel ID: "streaming_channel"
   - Channel name: "Camera Streaming"
   - Importance: HIGH

2. **Foreground Notification**:
   - Title: "Security Camera Active"
   - Content: "Streaming video to remote device"
   - Icon: Camera icon
   - Ongoing: true (cannot be dismissed)
   - Actions: Stop button

3. **Service Lifecycle**:
   ```kotlin
   class StreamingService : Service() {
       override fun onCreate() {
           super.onCreate()
           createNotificationChannel()
       }
       
       override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
           startForeground(NOTIFICATION_ID, createNotification())
           return START_STICKY
       }
       
       override fun onDestroy() {
           super.onDestroy()
           // Cleanup resources
       }
       
       override fun onBind(intent: Intent?): IBinder? = null
   }
   ```

4. **Service Type**:
   - Declare `foregroundServiceType="camera|microphone"` in manifest
   - Request `FOREGROUND_SERVICE_CAMERA` and `FOREGROUND_SERVICE_MICROPHONE` permissions

**Key Implementation Points**:
- Keep service running even when app is in background
- Handle service stop from notification action
- Acquire wake lock to prevent device sleep
- Properly release resources on destroy

---

## 🎨 Layout Files

### activity_main.xml

**Design Requirements**:
- Two Material Design 3 buttons centered vertically
- Modern card-based layout
- Proper spacing (16dp margins)
- Icons for each button (server/client icons)
- Clean gradient background (optional)

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="24dp">
    
    <!-- App Title -->
    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Security Camera"
        android:textSize="28sp"
        android:textStyle="bold"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="48dp" />
    
    <!-- Server Button -->
    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnServer"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Start as Server\n(Home Phone)"
        android:padding="20dp"
        android:textSize="16sp"
        app:icon="@android:drawable/ic_menu_camera"
        app:iconGravity="top"
        app:layout_constraintTop_toBottomOf="@id/tvTitle"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="64dp" />
    
    <!-- Client Button -->
    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnClient"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Connect as Client\n(Remote Phone)"
        android:padding="20dp"
        android:textSize="16sp"
        app:icon="@android:drawable/ic_menu_view"
        app:iconGravity="top"
        app:layout_constraintTop_toBottomOf="@id/btnServer"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="24dp" />
        
</androidx.constraintlayout.widget.ConstraintLayout>
```

### activity_server.xml

**Design Requirements**:
- Full-screen `SurfaceViewRenderer` for camera preview
- Overlay UI elements on top of preview:
  - Room code display (top center, large text)
  - Status message (below room code)
  - Stop button (bottom center)

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Camera Preview -->
    <org.webrtc.SurfaceViewRenderer
        android:id="@+id/surfaceView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    
    <!-- Overlay Container -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp"
        android:gravity="center"
        android:background="#80000000"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">
        
        <TextView
            android:text="Room Code:"
            android:textColor="@android:color/white"
            android:textSize="16sp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />
        
        <TextView
            android:id="@+id/tvRoomCode"
            android:text="123456"
            android:textColor="@android:color/white"
            android:textSize="48sp"
            android:textStyle="bold"
            android:letterSpacing="0.2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp" />
        
        <TextView
            android:id="@+id/tvStatus"
            android:text="Waiting for client..."
            android:textColor="@android:color/white"
            android:textSize="14sp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp" />
            
    </LinearLayout>
    
    <!-- Stop Button -->
    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnStop"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Stop Streaming"
        android:backgroundTint="@android:color/holo_red_dark"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginBottom="32dp" />
        
</androidx.constraintlayout.widget.ConstraintLayout>
```

### activity_client.xml

**Design Requirements**:
- Room code input section (initially visible)
- Full-screen video viewer (initially hidden)
- Dynamic UI state changes based on connection status

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Remote Video Stream (Initially Hidden) -->
    <org.webrtc.SurfaceViewRenderer
        android:id="@+id/surfaceView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="gone" />
    
    <!-- Connection Input Section -->
    <LinearLayout
        android:id="@+id/layoutInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="32dp"
        android:gravity="center"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">
        
        <TextView
            android:text="Enter Room Code"
            android:textSize="24sp"
            android:textStyle="bold"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginBottom="24dp" />
        
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:boxBackgroundMode="outline"
            android:hint="6-Digit Code">
            
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etRoomCode"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="number"
                android:maxLength="6"
                android:textSize="24sp"
                android:gravity="center" />
                
        </com.google.android.material.textfield.TextInputLayout>
        
        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnConnect"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Connect"
            android:padding="16dp"
            android:layout_marginTop="24dp" />
        
        <TextView
            android:id="@+id/tvStatus"
            android:text="Disconnected"
            android:textSize="14sp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp" />
            
    </LinearLayout>
    
    <!-- Disconnect Button (Initially Hidden) -->
    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnDisconnect"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Disconnect"
        android:backgroundTint="@android:color/holo_red_dark"
        android:visibility="gone"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginBottom="32dp" />
        
</androidx.constraintlayout.widget.ConstraintLayout>
```

---

## ⚡ Important Implementation Details

### 1. Runtime Permissions
- **Must request** at runtime before accessing camera/microphone
- Use `ActivityResultContracts.RequestMultiplePermissions()`
- Handle permission denial gracefully with clear user messages
- Explain why each permission is needed

### 2. WebRTC Initialization
- **Must call** `PeerConnectionFactory.initialize()` with application context
- Initialize on background thread to avoid blocking UI
- Configure audio/video encoders and decoders
- Set up proper thread management for WebRTC operations

### 3. Camera Configuration
- Use `Camera2Enumerator` for modern camera API
- Select rear camera for server, no camera for client
- Configure video constraints:
  ```kotlin
  width = 1280
  height = 720
  framerate = 30
  ```
- Handle camera rotation and orientation

### 4. Audio Configuration
- Enable speaker mode for audio playback
- Configure audio attributes for communication
- Enable echo cancellation and noise suppression
- Proper audio routing (speaker vs earpiece)

### 5. Threading Requirements
- WebRTC operations require specific threads:
  - **Signaling Thread**: For creating offers/answers
  - **Worker Thread**: For ICE operations
  - **Network Thread**: For network operations
- Use `Executors` to create thread pools
- Never block the main thread

### 6. Resource Cleanup
- **Critical**: Properly dispose all WebRTC objects to prevent memory leaks
- Cleanup order:
  1. Stop video/audio tracks
  2. Close peer connection
  3. Dispose peer connection factory
  4. Release camera
  5. Disconnect from signaling server

### 7. Error Handling
- Wrap WebRTC operations in try-catch blocks
- Show user-friendly error messages (not technical exceptions)
- Log errors for debugging
- Handle common scenarios:
  - Network disconnection
  - Camera permission denied
  - Invalid room code
  - Server unreachable

### 8. Network Handling
- Check network connectivity before connecting
- Implement reconnection logic with exponential backoff
- Handle ICE connection failures
- Monitor connection state changes

### 9. UI Updates
- Use `runOnUiThread()` for UI updates from WebRTC callbacks
- Use coroutines with `Dispatchers.Main` for async UI updates
- Show loading indicators during connection
- Update status messages in real-time

### 10. Battery Optimization
- Request battery optimization exemption (optional)
- Use wake lock judiciously (only when streaming)
- Release wake lock when streaming stops
- Optimize video encoding settings for battery life

---

## 🎁 Additional Features (Optional Enhancements)

Implement these features to enhance the user experience:

1. **Video Quality Indicator**
   - Display current video resolution and bitrate
   - Show network latency
   - Indicate poor connection with visual feedback

2. **Camera Toggle**
   - Button to switch between front and rear camera on server
   - Smooth transition without disconnecting stream

3. **Audio/Video Mute**
   - Toggle buttons for microphone and camera
   - Visual indication when muted
   - Notify remote peer of mute state

4. **Room Code Management**
   - Save recently used room codes
   - Quick connect to saved rooms
   - Room code sharing via system share sheet

5. **Authentication**
   - Optional PIN protection for rooms
   - Require PIN entry before joining
   - Server can accept/reject incoming connections

6. **Connection Statistics**
   - Display real-time statistics:
     - Bandwidth usage
     - Packet loss
     - Frame rate
     - Round-trip time (RTT)

7. **Screen Recording**
   - Record the received video stream
   - Save recordings to device storage
   - Request `WRITE_EXTERNAL_STORAGE` permission

8. **Motion Detection**
   - Detect motion in camera feed
   - Send notification to client when motion detected
   - Configurable sensitivity

9. **Multi-Client Support**
   - Allow multiple clients to connect to one server
   - Broadcast stream to all connected clients
   - Show list of connected clients

10. **Dark Theme Support**
    - Implement Material Design 3 theming
    - Respect system theme preference
    - Smooth theme transitions

---

## 💻 Code Style Guidelines

Follow these coding standards for clean, maintainable code:

### Kotlin Best Practices
```kotlin
// Use coroutines for async operations
viewModelScope.launch {
    withContext(Dispatchers.IO) {
        // Background work
    }
    // Update UI on main thread
}

// Use proper null safety
val result = data?.let { processData(it) } ?: defaultValue

// Use sealed classes for states
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val clientId: String) : ConnectionState()
    data class Failed(val error: String) : ConnectionState()
}
```

### Architecture
- **Use MVVM pattern** where appropriate:
  - Activities handle UI only
  - ViewModels manage state and business logic
  - Repository pattern for data sources
- **Separation of concerns**: Keep WebRTC logic separate from UI logic
- **Dependency injection**: Pass dependencies through constructors

### View Binding
```kotlin
// Use ViewBinding for type-safe view access
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnServer.setOnClickListener { /* ... */ }
    }
}
```

### Documentation
```kotlin
/**
 * Manages WebRTC peer connection for video streaming.
 * 
 * @param context Application context for WebRTC initialization
 * @param observer Callback observer for connection events
 */
class WebRTCClient(
    private val context: Context,
    private val observer: RTCPeerConnectionObserver
) {
    /**
     * Creates a WebRTC offer to initiate connection.
     * Must be called from signaling thread.
     */
    fun createOffer() { /* ... */ }
}
```

### Naming Conventions
- **Classes**: PascalCase (e.g., `WebRTCClient`)
- **Functions**: camelCase (e.g., `createOffer`)
- **Variables**: camelCase (e.g., `localVideoTrack`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `LOCAL_TRACK_ID`)
- **Resource IDs**: snake_case with prefix (e.g., `btn_connect`, `tv_status`)

### Error Handling
```kotlin
try {
    webRTCClient.createOffer()
} catch (e: Exception) {
    Log.e(TAG, "Failed to create offer", e)
    showError("Connection failed: ${e.message}")
}
```

---

## 🌐 Signaling Server

### Setup

Create a separate Node.js project for the signaling server:

**Directory Structure**:
```
signaling-server/
├── server.js
├── package.json
└── .env
```

### package.json

```json
{
  "name": "security-cam-signaling",
  "version": "1.0.0",
  "description": "WebRTC signaling server for security camera app",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js"
  },
  "keywords": ["webrtc", "signaling", "socket.io"],
  "author": "",
  "license": "MIT",
  "dependencies": {
    "express": "^4.18.2",
    "socket.io": "^4.6.1",
    "cors": "^2.8.5"
  },
  "devDependencies": {
    "nodemon": "^3.0.1"
  }
}
```

### server.js

**Requirements**:

1. **Server Setup**:
   - Express.js HTTP server
   - Socket.io for WebSocket communication
   - CORS enabled for cross-origin requests
   - Port: 3000 (configurable)

2. **Room Management**:
   - Store active rooms in memory: `Map<roomId, { server: socketId, clients: socketId[] }>`
   - Create room when server starts streaming
   - Allow clients to join existing rooms
   - Cleanup rooms when all participants disconnect

3. **Event Handlers**:

   **Server Events (Incoming)**:
   - `create-room`: Server creates room → Store room with server socket ID
   - `join-room`: Client joins room → Add client to room, notify server
   - `offer`: Forward WebRTC offer to target peer
   - `answer`: Forward WebRTC answer to target peer
   - `ice-candidate`: Forward ICE candidate to target peer
   - `disconnect`: Handle cleanup when socket disconnects

   **Client Events (Outgoing)**:
   - `room-created`: Confirm room creation with room ID
   - `room-joined`: Confirm client joined room
   - `client-joined`: Notify server that client joined
   - `offer`: Forward offer from server to client
   - `answer`: Forward answer from client to server
   - `ice-candidate`: Forward ICE candidate to peer
   - `error`: Send error messages (room not found, room full, etc.)

4. **Implementation**:

```javascript
const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');

const app = express();
app.use(cors());

const server = http.createServer(app);
const io = socketIo(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

// Store active rooms: { roomId: { server: socketId, clients: [socketIds] } }
const rooms = new Map();

io.on('connection', (socket) => {
    console.log(`[${new Date().toISOString()}] New connection: ${socket.id}`);
    
    // Server creates a new room
    socket.on('create-room', (roomId) => {
        console.log(`[${new Date().toISOString()}] Creating room: ${roomId} by ${socket.id}`);
        
        if (rooms.has(roomId)) {
            socket.emit('error', 'Room already exists');
            return;
        }
        
        rooms.set(roomId, { server: socket.id, clients: [] });
        socket.join(roomId);
        socket.emit('room-created', roomId);
    });
    
    // Client joins an existing room
    socket.on('join-room', (roomId) => {
        console.log(`[${new Date().toISOString()}] Client ${socket.id} joining room: ${roomId}`);
        
        const room = rooms.get(roomId);
        if (!room) {
            socket.emit('error', 'Room not found');
            return;
        }
        
        room.clients.push(socket.id);
        socket.join(roomId);
        socket.emit('room-joined', roomId);
        
        // Notify server that client joined
        io.to(room.server).emit('client-joined', socket.id);
    });
    
    // Forward offer
    socket.on('offer', ({ target, sdp }) => {
        console.log(`[${new Date().toISOString()}] Forwarding offer from ${socket.id} to ${target}`);
        io.to(target).emit('offer', { sender: socket.id, sdp });
    });
    
    // Forward answer
    socket.on('answer', ({ target, sdp }) => {
        console.log(`[${new Date().toISOString()}] Forwarding answer from ${socket.id} to ${target}`);
        io.to(target).emit('answer', { sender: socket.id, sdp });
    });
    
    // Forward ICE candidate
    socket.on('ice-candidate', ({ target, candidate }) => {
        console.log(`[${new Date().toISOString()}] Forwarding ICE candidate from ${socket.id} to ${target}`);
        io.to(target).emit('ice-candidate', { sender: socket.id, candidate });
    });
    
    // Handle disconnect
    socket.on('disconnect', () => {
        console.log(`[${new Date().toISOString()}] Disconnected: ${socket.id}`);
        
        // Clean up rooms
        for (const [roomId, room] of rooms.entries()) {
            if (room.server === socket.id) {
                // Server disconnected, remove room
                rooms.delete(roomId);
                console.log(`[${new Date().toISOString()}] Room ${roomId} deleted (server disconnected)`);
            } else {
                // Remove client from room
                const index = room.clients.indexOf(socket.id);
                if (index > -1) {
                    room.clients.splice(index, 1);
                    console.log(`[${new Date().toISOString()}] Client removed from room ${roomId}`);
                }
            }
        }
    });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`✅ Signaling server running on port ${PORT}`);
    console.log(`📡 Server URL: http://localhost:${PORT}`);
});
```

5. **Deployment**:
   - For local testing: Run on same network, use local IP
   - For production: Deploy to cloud (Heroku, DigitalOcean, AWS)
   - Use environment variables for configuration
   - Enable HTTPS for production (required for WebRTC)

6. **Logging**:
   - Log all socket events with timestamps
   - Log room creation/deletion
   - Log connection/disconnection events
   - Helps with debugging connection issues

7. **Error Handling**:
   - Validate room IDs before operations
   - Handle edge cases (room full, invalid data)
   - Send descriptive error messages to clients
   - Gracefully handle socket disconnections

---

## 🚀 Deployment Instructions

### Android App

1. **Setup Project**:
   ```bash
   # Create new Android Studio project
   # Select "Empty Activity" template
   # Set package name: com.securitycam
   # Minimum SDK: API 21 (Android 5.0)
   ```

2. **Add Dependencies**:
   - Copy `build.gradle.kts` configuration
   - Sync Gradle files
   - Resolve any dependency conflicts

3. **Update Configuration**:
   - Open [Constants.kt](Constants.kt)
   - Replace `YOUR_SERVER_IP` with your signaling server IP/domain:
     ```kotlin
     const val SIGNALING_SERVER_URL = "http://192.168.1.100:3000"
     // OR for production
     const val SIGNALING_SERVER_URL = "https://your-domain.com"
     ```

4. **Test on Devices**:
   - Install on two physical Android devices (emulators won't work for camera)
   - Ensure both devices are on the same network (for local testing)
   - Grant all required permissions

5. **Build APK**:
   ```bash
   # Debug build
   ./gradlew assembleDebug
   
   # Release build (requires signing)
   ./gradlew assembleRelease
   ```

### Signaling Server

1. **Install Dependencies**:
   ```bash
   cd signaling-server
   npm install
   ```

2. **Start Server**:
   ```bash
   # Development (with auto-restart)
   npm run dev
   
   # Production
   npm start
   ```

3. **Get Server IP**:
   ```bash
   # Windows
   ipconfig
   
   # Linux/Mac
   ifconfig
   
   # Look for IPv4 address (e.g., 192.168.1.100)
   ```

4. **Deploy to Production** (Optional):
   
   **Option A: Heroku**
   ```bash
   # Install Heroku CLI
   heroku create your-app-name
   git push heroku main
   ```
   
   **Option B: DigitalOcean**
   - Create droplet
   - SSH into server
   - Install Node.js
   - Clone repository
   - Run with PM2:
     ```bash
     npm install -g pm2
     pm2 start server.js
     pm2 save
     pm2 startup
     ```

5. **Enable HTTPS** (Required for production):
   - Obtain SSL certificate (Let's Encrypt)
   - Configure Nginx as reverse proxy
   - Update Android app to use `https://` URL

### Testing Checklist

- [ ] Permissions granted on both devices
- [ ] Signaling server running and accessible
- [ ] Server app generates room code
- [ ] Client app can join with room code
- [ ] Video stream displays on client
- [ ] Audio works on client
- [ ] Connection survives app backgrounding (foreground service)
- [ ] Cleanup works properly on disconnect
- [ ] Reconnection works after network interruption

---

## 🎯 Expected Behavior

### Server App (Home Phone)
1. Launch app → Select "Start as Server"
2. Camera preview appears immediately
3. Random 6-digit room code displayed
4. Status: "Waiting for client..."
5. When client connects → Status: "Client Connected"
6. Foreground notification appears
7. Stream continues even when app is backgrounded
8. Tap "Stop Streaming" → Stream ends, notification disappears

### Client App (Remote Phone)
1. Launch app → Select "Connect as Client"
2. Enter 6-digit room code from server
3. Tap "Connect" → Status: "Connecting..."
4. Stream appears in full screen → Status: "Connected"
5. Audio plays through speaker
6. Connection maintained during screen rotation
7. Tap "Disconnect" → Return to input screen

### Network Scenarios
- **Same WiFi**: Direct connection, lowest latency
- **Different Networks**: STUN servers enable NAT traversal
- **Poor Connection**: Video quality auto-adjusts
- **Connection Lost**: UI shows "Disconnected", allows reconnection

---

## 📝 Final Notes

### Common Issues & Solutions

1. **Camera not working**:
   - Check camera permission granted
   - Verify device has camera hardware
   - Check if another app is using camera

2. **No video stream**:
   - Verify signaling server is running and accessible
   - Check `SIGNALING_SERVER_URL` is correct
   - Review logcat for WebRTC errors
   - Ensure both devices are connected to internet

3. **Audio not working**:
   - Check microphone/speaker permissions
   - Verify audio routing configuration
   - Test with device volume up

4. **Connection fails**:
   - Check firewall settings
   - Verify ICE servers are reachable
   - Review signaling server logs
   - Test with TURN servers if STUN fails

5. **App crashes**:
   - Check for null pointer exceptions
   - Verify proper WebRTC initialization
   - Ensure resources are cleaned up
   - Review stack trace in logcat

### Performance Optimization

1. **Video Quality**:
   - Adjust resolution based on network conditions
   - Lower frame rate for slower connections
   - Use adaptive bitrate streaming

2. **Battery Life**:
   - Reduce video quality when on battery
   - Stop streaming when battery is low
   - Release wake lock when not needed

3. **Network Usage**:
   - Monitor data usage
   - Warn user on cellular data
   - Implement data-saving mode

### Security Considerations

⚠️ **Important**: This is a basic implementation for learning/personal use.

For production use, implement:
- End-to-end encryption (WebRTC supports this)
- Authentication/authorization for room access
- HTTPS for signaling server
- Secure room code generation
- Rate limiting on signaling server
- Input validation for room codes

---

## ✅ Implementation Checklist

Use this checklist to track your progress:

### Android App
- [ ] Project structure created
- [ ] Dependencies added to build.gradle.kts
- [ ] AndroidManifest.xml configured
- [ ] Constants.kt with server URL
- [ ] MainActivity.kt with role selection
- [ ] ServerActivity.kt with camera streaming
- [ ] ClientActivity.kt with stream viewing
- [ ] WebRTCClient.kt for WebRTC management
- [ ] SignalingClient.kt for Socket.io
- [ ] RTCPeerConnectionObserver.kt for callbacks
- [ ] StreamingService.kt for foreground service
- [ ] activity_main.xml layout
- [ ] activity_server.xml layout
- [ ] activity_client.xml layout
- [ ] Runtime permissions implementation
- [ ] Error handling throughout
- [ ] Resource cleanup in onDestroy

### Signaling Server
- [ ] Node.js project created
- [ ] package.json configured
- [ ] server.js implemented
- [ ] Socket.io events handled
- [ ] Room management logic
- [ ] Error handling
- [ ] Logging implemented
- [ ] Server tested locally

### Testing
- [ ] App builds without errors
- [ ] Permissions work correctly
- [ ] Server creates rooms successfully
- [ ] Client can join rooms
- [ ] Video streams properly
- [ ] Audio works correctly
- [ ] Foreground service runs
- [ ] Disconnect/cleanup works
- [ ] Tested on two physical devices

### Deployment
- [ ] Signaling server deployed
- [ ] HTTPS configured (if production)
- [ ] App updated with server URL
- [ ] Release APK built
- [ ] App tested in production

---

**END OF PROMPT**

---

## 🎯 How to Use This Prompt

1. **Copy the entire prompt above** (from "Native Android Security Camera App" to "END OF PROMPT")

2. **Paste into your AI assistant** (GitHub Copilot Chat, Cursor, etc.)

3. **Generate the code** - The AI will create all necessary files

4. **Set up the signaling server**:
   ```bash
   cd signaling-server
   npm install
   npm start
   ```

5. **Update the Android app** with your server IP in [Constants.kt](Constants.kt)

6. **Build and run** on two Android devices

7. **Test the connection** - Start server on one device, connect from the other

---

## 📞 Support

If you encounter issues:
- Check the logcat output in Android Studio
- Review signaling server logs
- Verify network connectivity
- Ensure all permissions are granted
- Test signaling server with a WebSocket client

---

**Good luck building your Security Camera app! 📱📹**