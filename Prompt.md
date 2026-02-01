# Native Android Security Camera App - Complete Implementation Guide

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Project Structure](#project-structure)
3. [Configuration](#configuration)
4. [Implementation Roadmap - TODO](#implementation-roadmap---todo)
5. [Core Components](#core-components)
6. [Signaling Server](#signaling-server)
7. [Deployment Instructions](#deployment-instructions)

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

## � Implementation Roadmap - TODO

Follow this step-by-step implementation guide to build the Security Camera app. Complete each main TODO and its sub-items in order.

### Phase 1: Project Setup & Configuration

#### TODO 1: Initialize Android Project
- [ ] Create new Android Studio project
  - [ ] Select "Empty Activity" template
  - [ ] Set package name to `com.securitycam`
  - [ ] Set minimum SDK to API 21 (Android 5.0)
  - [ ] Set target SDK to API 34
  - [ ] Choose Kotlin as programming language
- [ ] Configure project settings
  - [ ] Enable ViewBinding in build.gradle.kts
  - [ ] Set up Kotlin version 1.8
  - [ ] Configure ProGuard rules (if needed)

#### TODO 2: Add Dependencies
- [ ] Update app-level build.gradle.kts
  - [ ] Add WebRTC dependency (org.webrtc:google-webrtc:1.0.32006)
  - [ ] Add Socket.io dependency (io.socket:socket.io-client:2.1.0)
  - [ ] Add OkHttp dependency (com.squareup.okhttp3:okhttp:4.12.0)
  - [ ] Add Kotlin coroutines dependencies
  - [ ] Add AndroidX lifecycle dependencies
  - [ ] Add Gson dependency for JSON parsing
  - [ ] Add Material Design components
- [ ] Sync Gradle files
  - [ ] Resolve any dependency conflicts
  - [ ] Verify all dependencies are compatible

#### TODO 3: Configure Android Manifest
- [ ] Add required permissions
  - [ ] CAMERA permission
  - [ ] RECORD_AUDIO permission
  - [ ] INTERNET permission
  - [ ] ACCESS_NETWORK_STATE permission
  - [ ] MODIFY_AUDIO_SETTINGS permission
  - [ ] FOREGROUND_SERVICE permission
  - [ ] FOREGROUND_SERVICE_CAMERA permission (API 34+)
  - [ ] FOREGROUND_SERVICE_MICROPHONE permission (API 34+)
  - [ ] POST_NOTIFICATIONS permission (API 33+)
  - [ ] WAKE_LOCK permission
- [ ] Add hardware requirements
  - [ ] Camera hardware requirement
  - [ ] Camera autofocus feature
- [ ] Configure application settings
  - [ ] Enable cleartext traffic for development
  - [ ] Set app icon and label
  - [ ] Configure theme

#### TODO 4: Create Directory Structure
- [ ] Create package structure
  - [ ] Create `services` package
  - [ ] Create `webrtc` package
  - [ ] Create `utils` package
  - [ ] Create `models` package (optional, for data classes)
  - [ ] Create `ui` package (optional, if using fragments)
- [ ] Create resource directories
  - [ ] Verify `layout` directory exists
  - [ ] Verify `values` directory exists
  - [ ] Create `drawable` directory for icons (if needed)

---

### Phase 2: Utility Classes & Constants

#### TODO 5: Create Constants File
- [ ] Create `Constants.kt` in utils package
  - [ ] Define `SIGNALING_SERVER_URL` constant
  - [ ] Define `ICE_SERVERS` list with STUN servers
  - [ ] Define `LOCAL_TRACK_ID` constant
  - [ ] Define `LOCAL_STREAM_ID` constant
  - [ ] Add video quality constants (width, height, fps)
  - [ ] Add notification constants (channel ID, notification ID)
- [ ] Document all constants with comments

#### TODO 6: Create Utility Classes (Optional but Recommended)
- [ ] Create `PermissionUtils.kt`
  - [ ] Implement `hasPermission()` function
  - [ ] Implement `hasPermissions()` function
  - [ ] Add permission request helpers
- [ ] Create `NetworkUtils.kt`
  - [ ] Implement network connectivity check
  - [ ] Add network type detection (WiFi vs cellular)
- [ ] Create extension functions
  - [ ] String extensions for validation
  - [ ] View extensions for visibility
  - [ ] Context extensions for toasts/snackbars

---

### Phase 3: WebRTC Implementation

#### TODO 7: Implement RTCPeerConnectionObserver
- [ ] Create `RTCPeerConnectionObserver.kt`
  - [ ] Implement `PeerConnection.Observer` interface
  - [ ] Override `onIceCandidate()` method
  - [ ] Override `onAddStream()` method
  - [ ] Override `onIceConnectionChange()` method
  - [ ] Override `onSignalingChange()` method
  - [ ] Override `onIceGatheringChange()` method
  - [ ] Override `onRemoveStream()` method
  - [ ] Override `onDataChannel()` method
  - [ ] Override `onRenegotiationNeeded()` method
  - [ ] Add proper logging for all callbacks
- [ ] Define callback interfaces
  - [ ] Create callbacks for ICE candidates
  - [ ] Create callbacks for stream events
  - [ ] Create callbacks for connection state changes

#### TODO 8: Create WebRTC Factory (Recommended)
- [ ] Create `WebRTCFactory.kt`
  - [ ] Implement `createPeerConnectionFactory()` method
  - [ ] Implement `createPeerConnection()` method
  - [ ] Implement `createVideoEncoderFactory()` method
  - [ ] Implement `createVideoDecoderFactory()` method
  - [ ] Configure EglBase for video rendering
  - [ ] Set up ICE server configuration
- [ ] Add proper resource management
  - [ ] Implement cleanup methods
  - [ ] Handle factory disposal

#### TODO 9: Implement WebRTCClient
- [ ] Create `WebRTCClient.kt` class
  - [ ] Define constructor with context and observer
  - [ ] Declare private variables for WebRTC objects
- [ ] Implement initialization methods
  - [ ] `initializePeerConnectionFactory()`
    - [ ] Initialize WebRTC library with application context
    - [ ] Create PeerConnectionFactory with encoders/decoders
    - [ ] Set up audio and video codecs
  - [ ] `initializePeerConnection()`
    - [ ] Configure ICE servers
    - [ ] Set RTCConfiguration (bundle policy, RTCP mux, TCP candidate policy)
    - [ ] Create PeerConnection with observer
- [ ] Implement media track creation
  - [ ] `createLocalVideoTrack()`
    - [ ] Use Camera2Enumerator for camera selection
    - [ ] Create video capturer for rear camera
    - [ ] Set video constraints (resolution, fps)
    - [ ] Create video source and video track
  - [ ] `createLocalAudioTrack()`
    - [ ] Create audio source
    - [ ] Configure audio constraints (echo cancellation, noise suppression)
    - [ ] Create audio track
  - [ ] `addStreamToLocalPeer()`
    - [ ] Create MediaStream
    - [ ] Add video track to stream
    - [ ] Add audio track to stream
    - [ ] Add stream to peer connection
- [ ] Implement offer/answer methods
  - [ ] `createOffer()`
    - [ ] Set media constraints for offer
    - [ ] Create SDP offer
    - [ ] Set local description
    - [ ] Return offer via callback
  - [ ] `createAnswer()`
    - [ ] Set media constraints for answer
    - [ ] Create SDP answer
    - [ ] Set local description
    - [ ] Return answer via callback
  - [ ] `setRemoteDescription()`
    - [ ] Accept SessionDescription parameter
    - [ ] Set as remote description on peer connection
    - [ ] Handle success/error callbacks
- [ ] Implement ICE candidate handling
  - [ ] `addIceCandidate()`
    - [ ] Accept IceCandidate parameter
    - [ ] Add candidate to peer connection
    - [ ] Handle errors
- [ ] Implement control methods
  - [ ] `switchCamera()`
    - [ ] Get camera capturer
    - [ ] Switch between front and rear camera
  - [ ] `enableVideo(enable: Boolean)`
    - [ ] Enable or disable video track
  - [ ] `enableAudio(enable: Boolean)`
    - [ ] Enable or disable audio track
- [ ] Implement cleanup
  - [ ] `close()`
    - [ ] Stop all tracks
    - [ ] Close peer connection
    - [ ] Dispose peer connection factory
    - [ ] Release camera
    - [ ] Clean up all resources
- [ ] Add error handling throughout
  - [ ] Wrap operations in try-catch blocks
  - [ ] Log errors appropriately
  - [ ] Notify callbacks of errors

---

### Phase 4: Signaling Implementation

#### TODO 10: Implement SignalingClient
- [ ] Create `SignalingClient.kt` class
  - [ ] Define constructor with server URL
  - [ ] Declare Socket.io socket variable
- [ ] Define SignalingListener interface
  - [ ] `onRoomCreated(roomId: String)`
  - [ ] `onRoomJoined(roomId: String)`
  - [ ] `onClientJoined(clientId: String)`
  - [ ] `onOfferReceived(offer: SessionDescription)`
  - [ ] `onAnswerReceived(answer: SessionDescription)`
  - [ ] `onIceCandidateReceived(candidate: IceCandidate)`
  - [ ] `onError(error: String)`
  - [ ] `onDisconnected()`
- [ ] Implement connection methods
  - [ ] `connect()`
    - [ ] Initialize Socket.io client with URL
    - [ ] Set up socket options (timeouts, reconnection)
    - [ ] Connect to server
    - [ ] Set up event listeners
  - [ ] `disconnect()`
    - [ ] Remove all listeners
    - [ ] Disconnect socket
    - [ ] Clean up resources
- [ ] Implement socket event listeners
  - [ ] Listen for `room-created` event
  - [ ] Listen for `room-joined` event
  - [ ] Listen for `client-joined` event
  - [ ] Listen for `offer` event
  - [ ] Listen for `answer` event
  - [ ] Listen for `ice-candidate` event
  - [ ] Listen for `error` event
  - [ ] Listen for `disconnect` event
- [ ] Implement emit methods
  - [ ] `createRoom(roomId: String)`
    - [ ] Emit `create-room` event with room ID
  - [ ] `joinRoom(roomId: String)`
    - [ ] Emit `join-room` event with room ID
  - [ ] `sendOffer(target: String, offer: SessionDescription)`
    - [ ] Convert SessionDescription to JSON
    - [ ] Emit `offer` event with target and SDP
  - [ ] `sendAnswer(target: String, answer: SessionDescription)`
    - [ ] Convert SessionDescription to JSON
    - [ ] Emit `answer` event with target and SDP
  - [ ] `sendIceCandidate(target: String, candidate: IceCandidate)`
    - [ ] Convert IceCandidate to JSON
    - [ ] Emit `ice-candidate` event with target and candidate
- [ ] Add helper methods
  - [ ] JSON conversion helpers for WebRTC objects
  - [ ] Error handling helpers
- [ ] Implement proper error handling
  - [ ] Handle socket connection errors
  - [ ] Handle timeout errors
  - [ ] Notify listener of errors
- [ ] Add logging
  - [ ] Log all socket events
  - [ ] Log connection status changes
  - [ ] Log errors

---

### Phase 5: Activities Implementation

#### TODO 11: Implement MainActivity
- [ ] Create `MainActivity.kt`
  - [ ] Extend `AppCompatActivity`
  - [ ] Set up ViewBinding
- [ ] Initialize UI components
  - [ ] Bind server button
  - [ ] Bind client button
  - [ ] Set up click listeners
- [ ] Implement permission handling
  - [ ] Define required permissions array
  - [ ] Set up `ActivityResultLauncher` for permissions
  - [ ] Implement `checkAndRequestPermissions()` method
  - [ ] Handle permission results
    - [ ] Check if all permissions granted
    - [ ] Show rationale if needed
    - [ ] Handle denial gracefully
- [ ] Implement navigation
  - [ ] `navigateToServer()`
    - [ ] Check permissions first
    - [ ] Create Intent for ServerActivity
    - [ ] Start activity
  - [ ] `navigateToClient()`
    - [ ] Check permissions first
    - [ ] Create Intent for ClientActivity
    - [ ] Start activity
- [ ] Add UI feedback
  - [ ] Show toasts for permission denial
  - [ ] Show dialog explaining permission importance

#### TODO 12: Implement ServerActivity
- [ ] Create `ServerActivity.kt`
  - [ ] Extend `AppCompatActivity` or `BaseWebRTCActivity`
  - [ ] Set up ViewBinding
- [ ] Declare member variables
  - [ ] WebRTCClient instance
  - [ ] SignalingClient instance
  - [ ] SurfaceViewRenderer reference
  - [ ] Room code string
  - [ ] Remote peer ID string
- [ ] Initialize UI components in `onCreate()`
  - [ ] Set up content view with ViewBinding
  - [ ] Initialize SurfaceViewRenderer
    - [ ] Call `init()` with EglBase context
    - [ ] Set mirror to true for local preview
  - [ ] Bind room code TextView
  - [ ] Bind status TextView
  - [ ] Bind stop button with click listener
- [ ] Generate and display room code
  - [ ] Create `generateRoomCode()` method
    - [ ] Generate random 6-digit number
    - [ ] Convert to string
    - [ ] Return room code
  - [ ] Display room code in TextView
- [ ] Initialize WebRTC
  - [ ] Create `initializeWebRTC()` method
    - [ ] Initialize WebRTCClient
    - [ ] Initialize PeerConnectionFactory
    - [ ] Create local video track
    - [ ] Create local audio track
    - [ ] Render local video to SurfaceViewRenderer
    - [ ] Add tracks to peer connection
- [ ] Initialize Signaling
  - [ ] Create `initializeSignaling()` method
    - [ ] Create SignalingClient instance
    - [ ] Set listener callbacks
    - [ ] Connect to signaling server
    - [ ] Create room with generated room code
- [ ] Implement SignalingListener callbacks
  - [ ] `onRoomCreated(roomId)`
    - [ ] Update status to "Waiting for client..."
    - [ ] Log success
  - [ ] `onClientJoined(clientId)`
    - [ ] Store remote peer ID
    - [ ] Update status to "Client connecting..."
    - [ ] Create WebRTC offer
  - [ ] `onAnswerReceived(answer)`
    - [ ] Set remote description with answer
    - [ ] Update status to "Client connected"
    - [ ] Start foreground service
  - [ ] `onIceCandidateReceived(candidate)`
    - [ ] Add ICE candidate to peer connection
  - [ ] `onError(error)`
    - [ ] Display error message
    - [ ] Log error
- [ ] Implement WebRTC callbacks
  - [ ] Handle ICE candidate generation
    - [ ] Send via signaling client
  - [ ] Handle connection state changes
    - [ ] Update UI based on state
    - [ ] Handle disconnection
- [ ] Implement offer creation
  - [ ] Create `createOffer()` method
    - [ ] Call WebRTCClient.createOffer()
    - [ ] Send offer via SignalingClient
- [ ] Implement foreground service start
  - [ ] Create Intent for StreamingService
  - [ ] Pass necessary data (room code, client ID)
  - [ ] Start foreground service
- [ ] Implement stop streaming
  - [ ] Stop foreground service
  - [ ] Close WebRTC connection
  - [ ] Disconnect from signaling server
  - [ ] Finish activity
- [ ] Implement lifecycle methods
  - [ ] `onDestroy()`
    - [ ] Release SurfaceViewRenderer
    - [ ] Close WebRTCClient
    - [ ] Disconnect SignalingClient
    - [ ] Stop service
    - [ ] Clean up all resources
- [ ] Add error handling
  - [ ] Try-catch blocks around WebRTC operations
  - [ ] User-friendly error messages

#### TODO 13: Implement ClientActivity
- [ ] Create `ClientActivity.kt`
  - [ ] Extend `AppCompatActivity` or `BaseWebRTCActivity`
  - [ ] Set up ViewBinding
- [ ] Declare member variables
  - [ ] WebRTCClient instance
  - [ ] SignalingClient instance
  - [ ] SurfaceViewRenderer reference
  - [ ] Input room code EditText
  - [ ] Remote peer ID string
- [ ] Initialize UI components in `onCreate()`
  - [ ] Set up content view with ViewBinding
  - [ ] Initialize SurfaceViewRenderer (hidden initially)
    - [ ] Call `init()` with EglBase context
    - [ ] Set mirror to false for remote stream
  - [ ] Bind room code EditText
  - [ ] Bind connect button with click listener
  - [ ] Bind disconnect button (hidden initially)
  - [ ] Bind status TextView
  - [ ] Bind input layout container
- [ ] Implement connect button click
  - [ ] Validate room code
    - [ ] Check if 6 digits
    - [ ] Check if not empty
    - [ ] Show error if invalid
  - [ ] Show connecting status
  - [ ] Disable connect button
  - [ ] Initialize WebRTC
  - [ ] Initialize Signaling
  - [ ] Join room
- [ ] Initialize WebRTC
  - [ ] Create `initializeWebRTC()` method
    - [ ] Initialize WebRTCClient
    - [ ] Initialize PeerConnectionFactory
    - [ ] Create peer connection (no local tracks for client)
- [ ] Initialize Signaling
  - [ ] Create `initializeSignaling()` method
    - [ ] Create SignalingClient instance
    - [ ] Set listener callbacks
    - [ ] Connect to signaling server
    - [ ] Join room with entered room code
- [ ] Implement SignalingListener callbacks
  - [ ] `onRoomJoined(roomId)`
    - [ ] Update status to "Waiting for stream..."
    - [ ] Log success
  - [ ] `onOfferReceived(offer)`
    - [ ] Set remote description with offer
    - [ ] Create answer
    - [ ] Send answer via signaling client
  - [ ] `onIceCandidateReceived(candidate)`
    - [ ] Add ICE candidate to peer connection
  - [ ] `onError(error)`
    - [ ] Display error message
    - [ ] Re-enable connect button
    - [ ] Show input layout
- [ ] Implement WebRTC callbacks
  - [ ] Handle ICE candidate generation
    - [ ] Send via signaling client
  - [ ] Handle remote stream added
    - [ ] Render stream to SurfaceViewRenderer
    - [ ] Show video surface, hide input layout
    - [ ] Update status to "Connected"
    - [ ] Show disconnect button
  - [ ] Handle connection state changes
    - [ ] Update status TextView
    - [ ] Handle disconnection
- [ ] Implement answer creation
  - [ ] Create `createAnswer()` method
    - [ ] Call WebRTCClient.createAnswer()
    - [ ] Send answer via SignalingClient
- [ ] Implement UI state management
  - [ ] Create `showConnectedState()` method
    - [ ] Show SurfaceViewRenderer
    - [ ] Hide input layout
    - [ ] Show disconnect button
    - [ ] Update status
  - [ ] Create `showDisconnectedState()` method
    - [ ] Hide SurfaceViewRenderer
    - [ ] Show input layout
    - [ ] Hide disconnect button
    - [ ] Clear room code
    - [ ] Enable connect button
- [ ] Implement disconnect button click
  - [ ] Close WebRTC connection
  - [ ] Disconnect from signaling server
  - [ ] Show disconnected state
- [ ] Implement lifecycle methods
  - [ ] `onDestroy()`
    - [ ] Release SurfaceViewRenderer
    - [ ] Close WebRTCClient
    - [ ] Disconnect SignalingClient
    - [ ] Clean up all resources
- [ ] Add error handling
  - [ ] Try-catch blocks around WebRTC operations
  - [ ] User-friendly error messages
  - [ ] Handle network errors gracefully

---

### Phase 6: Foreground Service Implementation

#### TODO 14: Implement StreamingService
- [ ] Create `StreamingService.kt`
  - [ ] Extend `Service` class
- [ ] Define constants
  - [ ] Notification channel ID
  - [ ] Notification ID
  - [ ] Channel name and description
- [ ] Declare member variables
  - [ ] WakeLock reference
  - [ ] Notification reference
- [ ] Implement `onCreate()`
  - [ ] Call super
  - [ ] Create notification channel (for Android O+)
  - [ ] Log service creation
- [ ] Implement notification channel creation
  - [ ] Create `createNotificationChannel()` method
    - [ ] Check Android version (O+)
    - [ ] Create NotificationChannel with ID, name, importance
    - [ ] Set description
    - [ ] Register channel with NotificationManager
- [ ] Implement notification creation
  - [ ] Create `createNotification()` method
    - [ ] Build notification with NotificationCompat.Builder
    - [ ] Set small icon (camera icon)
    - [ ] Set title: "Security Camera Active"
    - [ ] Set content text: "Streaming video to remote device"
    - [ ] Set ongoing to true (non-dismissible)
    - [ ] Set priority to HIGH
    - [ ] Create stop action PendingIntent
    - [ ] Add stop action to notification
    - [ ] Return notification
- [ ] Implement `onStartCommand()`
  - [ ] Create notification
  - [ ] Call `startForeground()` with notification ID and notification
  - [ ] Acquire wake lock
    - [ ] Get PowerManager
    - [ ] Create partial wake lock
    - [ ] Acquire with timeout
  - [ ] Log service start
  - [ ] Return `START_STICKY` to restart if killed
- [ ] Implement wake lock management
  - [ ] Create `acquireWakeLock()` method
    - [ ] Check if not already held
    - [ ] Acquire with timeout (e.g., 1 hour)
  - [ ] Create `releaseWakeLock()` method
    - [ ] Check if held
    - [ ] Release wake lock safely
- [ ] Implement `onDestroy()`
  - [ ] Release wake lock
  - [ ] Stop foreground
  - [ ] Log service destruction
  - [ ] Call super
- [ ] Implement `onBind()`
  - [ ] Return null (unbound service)
- [ ] Handle stop action from notification
  - [ ] Create broadcast receiver for stop action
  - [ ] Stop service when action received
- [ ] Add proper error handling
  - [ ] Try-catch around wake lock operations
  - [ ] Handle notification creation failures

---

### Phase 7: Layout Implementation

#### TODO 15: Create activity_main.xml
- [ ] Design main screen layout
  - [ ] Use ConstraintLayout as root
  - [ ] Add padding (24dp)
- [ ] Add app title TextView
  - [ ] ID: `tvTitle`
  - [ ] Text: "Security Camera"
  - [ ] Text size: 28sp
  - [ ] Text style: bold
  - [ ] Constrain to top center
  - [ ] Top margin: 48dp
- [ ] Add server button
  - [ ] Use MaterialButton
  - [ ] ID: `btnServer`
  - [ ] Text: "Start as Server\n(Home Phone)"
  - [ ] Set icon (camera icon)
  - [ ] Icon gravity: top
  - [ ] Padding: 20dp
  - [ ] Text size: 16sp
  - [ ] Constrain below title with top margin (64dp)
  - [ ] Match parent width
- [ ] Add client button
  - [ ] Use MaterialButton
  - [ ] ID: `btnClient`
  - [ ] Text: "Connect as Client\n(Remote Phone)"
  - [ ] Set icon (view icon)
  - [ ] Icon gravity: top
  - [ ] Padding: 20dp
  - [ ] Text size: 16sp
  - [ ] Constrain below server button with top margin (24dp)
  - [ ] Match parent width

#### TODO 16: Create activity_server.xml
- [ ] Design server screen layout
  - [ ] Use ConstraintLayout as root
- [ ] Add SurfaceViewRenderer for camera preview
  - [ ] ID: `surfaceView`
  - [ ] Match parent width and height
  - [ ] Constrain to fill entire screen
- [ ] Add overlay container for UI elements
  - [ ] Use LinearLayout with vertical orientation
  - [ ] Padding: 16dp
  - [ ] Background: semi-transparent black (#80000000)
  - [ ] Constrain to top of screen
  - [ ] Center horizontally
- [ ] Add room code label inside overlay
  - [ ] TextView with text "Room Code:"
  - [ ] Text color: white
  - [ ] Text size: 16sp
  - [ ] Center horizontally
- [ ] Add room code display inside overlay
  - [ ] TextView with ID: `tvRoomCode`
  - [ ] Text: "123456" (placeholder)
  - [ ] Text color: white
  - [ ] Text size: 48sp
  - [ ] Text style: bold
  - [ ] Letter spacing: 0.2
  - [ ] Top margin: 8dp
  - [ ] Center horizontally
- [ ] Add status TextView inside overlay
  - [ ] ID: `tvStatus`
  - [ ] Text: "Waiting for client..." (placeholder)
  - [ ] Text color: white
  - [ ] Text size: 14sp
  - [ ] Top margin: 8dp
  - [ ] Center horizontally
- [ ] Add stop button at bottom
  - [ ] Use MaterialButton
  - [ ] ID: `btnStop`
  - [ ] Text: "Stop Streaming"
  - [ ] Background tint: red
  - [ ] Constrain to bottom center
  - [ ] Bottom margin: 32dp

#### TODO 17: Create activity_client.xml
- [ ] Design client screen layout
  - [ ] Use ConstraintLayout as root
- [ ] Add SurfaceViewRenderer for remote stream
  - [ ] ID: `surfaceView`
  - [ ] Match parent width and height
  - [ ] Initially hidden (visibility: gone)
- [ ] Add input section container
  - [ ] Use LinearLayout with vertical orientation
  - [ ] ID: `layoutInput`
  - [ ] Padding: 32dp
  - [ ] Center in parent vertically and horizontally
  - [ ] Initially visible
- [ ] Add title TextView inside input container
  - [ ] Text: "Enter Room Code"
  - [ ] Text size: 24sp
  - [ ] Text style: bold
  - [ ] Center horizontally
  - [ ] Bottom margin: 24dp
- [ ] Add TextInputLayout for room code
  - [ ] Use Material TextInputLayout
  - [ ] Box background mode: outline
  - [ ] Hint: "6-Digit Code"
  - [ ] Match parent width
- [ ] Add TextInputEditText inside TextInputLayout
  - [ ] ID: `etRoomCode`
  - [ ] Input type: number
  - [ ] Max length: 6
  - [ ] Text size: 24sp
  - [ ] Gravity: center
  - [ ] Match parent width
- [ ] Add connect button
  - [ ] Use MaterialButton
  - [ ] ID: `btnConnect`
  - [ ] Text: "Connect"
  - [ ] Padding: 16dp
  - [ ] Top margin: 24dp
  - [ ] Match parent width
- [ ] Add status TextView
  - [ ] ID: `tvStatus`
  - [ ] Text: "Disconnected" (placeholder)
  - [ ] Text size: 14sp
  - [ ] Top margin: 16dp
  - [ ] Center horizontally
- [ ] Add disconnect button at bottom
  - [ ] Use MaterialButton
  - [ ] ID: `btnDisconnect`
  - [ ] Text: "Disconnect"
  - [ ] Background tint: red
  - [ ] Constrain to bottom center
  - [ ] Bottom margin: 32dp
  - [ ] Initially hidden (visibility: gone)

---

### Phase 8: Signaling Server Implementation

#### TODO 18: Set Up Node.js Project
- [ ] Create `signaling-server` directory
- [ ] Initialize npm project
  - [ ] Run `npm init -y`
  - [ ] Update package.json with project details
- [ ] Install dependencies
  - [ ] `npm install express`
  - [ ] `npm install socket.io`
  - [ ] `npm install cors`
- [ ] Install dev dependencies
  - [ ] `npm install --save-dev nodemon`
- [ ] Update package.json scripts
  - [ ] Add `"start": "node server.js"`
  - [ ] Add `"dev": "nodemon server.js"`

#### TODO 19: Implement server.js
- [ ] Set up Express server
  - [ ] Import required modules (express, http, socket.io, cors)
  - [ ] Create Express app
  - [ ] Enable CORS
  - [ ] Create HTTP server with Express app
  - [ ] Initialize Socket.io with CORS configuration
- [ ] Create rooms data structure
  - [ ] Declare `rooms` Map to store active rooms
  - [ ] Structure: `{ roomId: { server: socketId, clients: [socketIds] } }`
- [ ] Implement Socket.io connection handler
  - [ ] Log new connections with timestamp
- [ ] Implement `create-room` event handler
  - [ ] Accept roomId parameter
  - [ ] Check if room already exists
    - [ ] If exists, emit error
    - [ ] If not, create room with server socket ID
  - [ ] Add socket to room
  - [ ] Emit `room-created` confirmation
  - [ ] Log room creation
- [ ] Implement `join-room` event handler
  - [ ] Accept roomId parameter
  - [ ] Check if room exists
    - [ ] If not, emit error
    - [ ] If exists, add client to room's clients array
  - [ ] Add socket to room
  - [ ] Emit `room-joined` confirmation to client
  - [ ] Emit `client-joined` to server with client socket ID
  - [ ] Log client joining
- [ ] Implement `offer` event handler
  - [ ] Accept target and sdp parameters
  - [ ] Forward offer to target socket
  - [ ] Include sender socket ID
  - [ ] Log forwarding
- [ ] Implement `answer` event handler
  - [ ] Accept target and sdp parameters
  - [ ] Forward answer to target socket
  - [ ] Include sender socket ID
  - [ ] Log forwarding
- [ ] Implement `ice-candidate` event handler
  - [ ] Accept target and candidate parameters
  - [ ] Forward ICE candidate to target socket
  - [ ] Include sender socket ID
  - [ ] Log forwarding
- [ ] Implement `disconnect` event handler
  - [ ] Log disconnection with timestamp
  - [ ] Clean up rooms
    - [ ] If socket is a server, delete entire room
    - [ ] If socket is a client, remove from clients array
    - [ ] Log room cleanup
- [ ] Start server
  - [ ] Listen on port (default 3000, or from environment)
  - [ ] Log server start with URL
- [ ] Add comprehensive error handling
  - [ ] Validate incoming data
  - [ ] Handle malformed requests
  - [ ] Log all errors
- [ ] Add detailed logging
  - [ ] Log all socket events with timestamps
  - [ ] Log room state changes
  - [ ] Use consistent log format

---

### Phase 9: Testing & Debugging

#### TODO 20: Unit Testing (Optional but Recommended)
- [ ] Set up testing framework
  - [ ] Add JUnit dependencies
  - [ ] Add Mockito for mocking
  - [ ] Configure test directory
- [ ] Test WebRTCClient
  - [ ] Test initialization
  - [ ] Test offer/answer creation
  - [ ] Test ICE candidate handling
  - [ ] Test cleanup
- [ ] Test SignalingClient
  - [ ] Test connection/disconnection
  - [ ] Test event emission
  - [ ] Test event listening
  - [ ] Test error handling
- [ ] Test utility classes
  - [ ] Test permission checks
  - [ ] Test network checks
  - [ ] Test validation functions

#### TODO 21: Integration Testing
- [ ] Test signaling server locally
  - [ ] Start server with `npm start`
  - [ ] Verify server is listening on port 3000
  - [ ] Test with WebSocket client tool
  - [ ] Verify room creation
  - [ ] Verify room joining
  - [ ] Verify message forwarding
- [ ] Test Android app on emulator/device
  - [ ] Build and install app
  - [ ] Verify app launches without crashes
  - [ ] Test permission requests
  - [ ] Verify UI navigation
- [ ] Test WebRTC initialization
  - [ ] Launch ServerActivity
  - [ ] Verify camera preview appears
  - [ ] Verify room code is generated
  - [ ] Check logcat for WebRTC initialization logs
- [ ] Test end-to-end connection
  - [ ] Start signaling server
  - [ ] Get server's local IP address
  - [ ] Update SIGNALING_SERVER_URL in Constants.kt
  - [ ] Build and install on two physical devices
  - [ ] Launch server on Device 1
    - [ ] Verify camera preview
    - [ ] Note room code
  - [ ] Launch client on Device 2
    - [ ] Enter room code from Device 1
    - [ ] Tap Connect
    - [ ] Verify remote stream appears
    - [ ] Verify audio works
  - [ ] Test disconnection
    - [ ] Tap Disconnect on client
    - [ ] Verify UI returns to input state
    - [ ] Tap Stop on server
    - [ ] Verify service stops

#### TODO 22: Debugging Common Issues
- [ ] WebRTC issues
  - [ ] Check PeerConnectionFactory initialization
  - [ ] Verify ICE servers are accessible
  - [ ] Check SDP offer/answer exchange in logs
  - [ ] Verify ICE candidates are being exchanged
  - [ ] Check camera and microphone permissions
- [ ] Signaling issues
  - [ ] Verify server is running and accessible
  - [ ] Check Socket.io connection status
  - [ ] Verify room creation/joining events
  - [ ] Check for network connectivity
  - [ ] Verify SIGNALING_SERVER_URL is correct
- [ ] UI issues
  - [ ] Check ViewBinding setup
  - [ ] Verify layout files are correct
  - [ ] Check view visibility states
  - [ ] Test on different screen sizes
- [ ] Performance issues
  - [ ] Check for memory leaks (use Android Profiler)
  - [ ] Verify resources are properly released
  - [ ] Monitor network usage
  - [ ] Check battery consumption

---

### Phase 10: Optimization & Polish

#### TODO 23: Code Refactoring
- [ ] Review SOLID principles compliance
  - [ ] Ensure single responsibility for each class
  - [ ] Check for proper dependency injection
  - [ ] Verify interface segregation
- [ ] Apply DRY principle
  - [ ] Extract duplicate code to utility functions
  - [ ] Create base classes for common functionality
  - [ ] Use extension functions
- [ ] Follow KISS principle
  - [ ] Simplify complex methods
  - [ ] Break down large functions
  - [ ] Use clear naming conventions
- [ ] Add comprehensive documentation
  - [ ] Add KDoc comments to public methods
  - [ ] Document complex logic
  - [ ] Add usage examples in comments

#### TODO 24: UI/UX Improvements
- [ ] Add loading indicators
  - [ ] Show progress during connection
  - [ ] Add shimmer effect while initializing
- [ ] Improve error messages
  - [ ] Make messages user-friendly
  - [ ] Provide actionable suggestions
  - [ ] Use Snackbars instead of Toasts where appropriate
- [ ] Add visual feedback
  - [ ] Button press animations
  - [ ] Status color indicators (red/yellow/green)
  - [ ] Connection quality indicator
- [ ] Implement Material Design 3
  - [ ] Use Material colors and typography
  - [ ] Add elevation and shadows
  - [ ] Implement dark theme support
- [ ] Add accessibility features
  - [ ] Content descriptions for images
  - [ ] Proper focus order
  - [ ] Screen reader support

#### TODO 25: Performance Optimization
- [ ] Optimize video quality
  - [ ] Implement adaptive bitrate
  - [ ] Add quality presets (low/medium/high)
  - [ ] Auto-adjust based on network speed
- [ ] Reduce battery consumption
  - [ ] Optimize wake lock usage
  - [ ] Lower video quality on low battery
  - [ ] Add battery saver mode
- [ ] Optimize memory usage
  - [ ] Fix memory leaks (use LeakCanary)
  - [ ] Properly dispose Bitmaps
  - [ ] Clear caches when appropriate
- [ ] Improve startup time
  - [ ] Lazy initialize heavy objects
  - [ ] Use coroutines for background tasks
  - [ ] Defer non-critical initialization

#### TODO 26: Security Enhancements
- [ ] Implement HTTPS for signaling
  - [ ] Obtain SSL certificate
  - [ ] Configure server for HTTPS
  - [ ] Update Android app to use HTTPS URL
- [ ] Add room authentication
  - [ ] Implement PIN/password for rooms
  - [ ] Add room expiration
  - [ ] Limit number of join attempts
- [ ] Validate inputs
  - [ ] Sanitize room codes
  - [ ] Validate signaling messages
  - [ ] Prevent injection attacks
- [ ] Implement encryption
  - [ ] Enable DTLS for WebRTC
  - [ ] Verify SRTP is enabled
  - [ ] Add end-to-end encryption option

---

### Phase 11: Production Deployment

#### TODO 27: Prepare for Release
- [ ] Update app configuration
  - [ ] Set proper version code and version name
  - [ ] Update applicationId if needed
  - [ ] Set proper app name and icon
- [ ] Configure ProGuard
  - [ ] Add ProGuard rules for WebRTC
  - [ ] Add rules for Socket.io
  - [ ] Add rules for Gson
  - [ ] Test with minification enabled
- [ ] Create release build
  - [ ] Generate signing key
  - [ ] Configure signing in build.gradle
  - [ ] Build release APK/AAB
  - [ ] Test release build thoroughly
- [ ] Prepare store listing (if publishing)
  - [ ] Create app screenshots
  - [ ] Write app description
  - [ ] Create feature graphic
  - [ ] Add privacy policy URL
  - [ ] Complete content rating questionnaire

#### TODO 28: Deploy Signaling Server
- [ ] Choose hosting platform
  - [ ] Heroku (easy, free tier available)
  - [ ] DigitalOcean (more control, requires setup)
  - [ ] AWS/GCP (scalable, more complex)
  - [ ] VPS provider (custom setup)
- [ ] Deploy to production
  - [ ] Set up production environment
  - [ ] Configure environment variables
  - [ ] Deploy server code
  - [ ] Start server with process manager (PM2)
- [ ] Configure domain and SSL
  - [ ] Point domain to server IP
  - [ ] Install SSL certificate (Let's Encrypt)
  - [ ] Configure Nginx reverse proxy
  - [ ] Test HTTPS connection
- [ ] Monitor server
  - [ ] Set up logging
  - [ ] Configure error alerts
  - [ ] Monitor resource usage
  - [ ] Set up automatic restarts

#### TODO 29: Update Android App with Production URL
- [ ] Update Constants.kt
  - [ ] Change SIGNALING_SERVER_URL to production URL
  - [ ] Ensure URL uses HTTPS
  - [ ] Update any hardcoded values
- [ ] Test with production server
  - [ ] Build new release
  - [ ] Test on multiple devices
  - [ ] Verify connectivity
  - [ ] Test all features end-to-end
- [ ] Submit update or initial release
  - [ ] Upload to Google Play Console
  - [ ] Complete all required information
  - [ ] Submit for review

---

### Phase 12: Monitoring & Maintenance

#### TODO 30: Post-Launch Monitoring
- [ ] Monitor app performance
  - [ ] Check crash reports (Firebase Crashlytics)
  - [ ] Monitor ANR (Application Not Responding) rates
  - [ ] Review performance metrics
- [ ] Monitor signaling server
  - [ ] Check server logs regularly
  - [ ] Monitor uptime
  - [ ] Track resource usage
  - [ ] Set up alerts for downtime
- [ ] Gather user feedback
  - [ ] Monitor reviews and ratings
  - [ ] Collect bug reports
  - [ ] Track feature requests
- [ ] Plan updates
  - [ ] Prioritize bug fixes
  - [ ] Schedule feature additions
  - [ ] Plan performance improvements

---

## �🔧 Core Components

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

## 🏗️ Software Engineering Principles & Design Patterns

This section outlines essential software engineering principles and design patterns to follow throughout the project.

### SOLID Principles

#### 1. Single Responsibility Principle (SRP)
Each class should have one reason to change. Apply this throughout the project:

**Examples**:
```kotlin
// ❌ BAD - Class has multiple responsibilities
class ServerActivity : AppCompatActivity() {
    fun initializeCamera() { }
    fun createWebRTCConnection() { }
    fun handleSignaling() { }
    fun updateUI() { }
    fun managePermissions() { }
}

// ✅ GOOD - Separated responsibilities
class ServerActivity : AppCompatActivity() {
    private val cameraManager: CameraManager
    private val webRTCClient: WebRTCClient
    private val signalingClient: SignalingClient
    private val permissionHandler: PermissionHandler
    
    fun updateUI() { }  // Activity only handles UI
}

class CameraManager {
    fun initializeCamera() { }
    fun switchCamera() { }
    fun releaseCamera() { }
}

class PermissionHandler(private val activity: Activity) {
    fun requestPermissions() { }
    fun checkPermissions(): Boolean { }
}
```

**Apply SRP to**:
- **Activities**: Handle only UI updates and user interactions
- **WebRTCClient**: Handle only WebRTC peer connection logic
- **SignalingClient**: Handle only Socket.io signaling
- **StreamingService**: Handle only foreground service lifecycle
- **Managers**: Each manager handles one specific domain (camera, audio, permissions)

#### 2. Open/Closed Principle (OCP)
Classes should be open for extension but closed for modification.

**Examples**:
```kotlin
// ✅ Use interfaces for extensibility
interface MediaDevice {
    fun initialize()
    fun release()
    fun isAvailable(): Boolean
}

class CameraDevice : MediaDevice {
    override fun initialize() { /* Camera-specific logic */ }
    override fun release() { /* Camera cleanup */ }
    override fun isAvailable(): Boolean { /* Check camera */ }
}

class MicrophoneDevice : MediaDevice {
    override fun initialize() { /* Mic-specific logic */ }
    override fun release() { /* Mic cleanup */ }
    override fun isAvailable(): Boolean { /* Check mic */ }
}

// Easy to add new device types without modifying existing code
class ScreenCaptureDevice : MediaDevice {
    override fun initialize() { /* Screen capture logic */ }
    override fun release() { /* Screen capture cleanup */ }
    override fun isAvailable(): Boolean { /* Check screen capture */ }
}
```

#### 3. Liskov Substitution Principle (LSP)
Derived classes must be substitutable for their base classes.

**Examples**:
```kotlin
// ✅ GOOD - Subtypes can replace base type
interface StreamRenderer {
    fun renderStream(stream: MediaStream)
    fun clearStream()
}

class LocalStreamRenderer(private val surfaceView: SurfaceViewRenderer) : StreamRenderer {
    override fun renderStream(stream: MediaStream) {
        stream.videoTracks.firstOrNull()?.addSink(surfaceView)
    }
    
    override fun clearStream() {
        surfaceView.release()
    }
}

class RemoteStreamRenderer(private val surfaceView: SurfaceViewRenderer) : StreamRenderer {
    override fun renderStream(stream: MediaStream) {
        stream.videoTracks.firstOrNull()?.addSink(surfaceView)
    }
    
    override fun clearStream() {
        surfaceView.release()
    }
}

// Both can be used interchangeably
fun setupRenderer(renderer: StreamRenderer, stream: MediaStream) {
    renderer.renderStream(stream)
}
```

#### 4. Interface Segregation Principle (ISP)
Clients should not be forced to depend on interfaces they don't use.

**Examples**:
```kotlin
// ❌ BAD - Fat interface
interface MediaController {
    fun startVideo()
    fun stopVideo()
    fun startAudio()
    fun stopAudio()
    fun switchCamera()
    fun adjustZoom(level: Float)
}

// ✅ GOOD - Segregated interfaces
interface VideoController {
    fun startVideo()
    fun stopVideo()
    fun switchCamera()
}

interface AudioController {
    fun startAudio()
    fun stopAudio()
}

interface CameraController {
    fun adjustZoom(level: Float)
    fun setFocus(x: Float, y: Float)
}

// Classes implement only what they need
class BasicWebRTCClient : VideoController, AudioController {
    override fun startVideo() { }
    override fun stopVideo() { }
    override fun startAudio() { }
    override fun stopAudio() { }
}

class AdvancedCameraManager : VideoController, CameraController {
    override fun startVideo() { }
    override fun stopVideo() { }
    override fun switchCamera() { }
    override fun adjustZoom(level: Float) { }
    override fun setFocus(x: Float, y: Float) { }
}
```

#### 5. Dependency Inversion Principle (DIP)
Depend on abstractions, not concretions. Use dependency injection.

**Examples**:
```kotlin
// ❌ BAD - Direct dependency on concrete class
class ServerActivity : AppCompatActivity() {
    private val webRTCClient = WebRTCClient()  // Tight coupling
    private val signalingClient = SignalingClient()  // Hard to test
}

// ✅ GOOD - Dependency injection with interfaces
interface IWebRTCClient {
    fun createOffer()
    fun createAnswer()
    fun addIceCandidate(candidate: IceCandidate)
}

interface ISignalingClient {
    fun connect()
    fun disconnect()
    fun sendOffer(offer: SessionDescription)
}

class ServerActivity : AppCompatActivity() {
    private lateinit var webRTCClient: IWebRTCClient
    private lateinit var signalingClient: ISignalingClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inject dependencies (can use Dagger/Hilt for this)
        webRTCClient = WebRTCClient(applicationContext, observer)
        signalingClient = SignalingClient(Constants.SIGNALING_SERVER_URL)
    }
}

// Easy to mock for testing
class MockWebRTCClient : IWebRTCClient {
    override fun createOffer() { /* Mock implementation */ }
    override fun createAnswer() { /* Mock implementation */ }
    override fun addIceCandidate(candidate: IceCandidate) { }
}
```

---

### DRY (Don't Repeat Yourself)

Eliminate code duplication by extracting common logic into reusable components.

**Examples**:

```kotlin
// ❌ BAD - Repeated code
class ServerActivity : AppCompatActivity() {
    fun setupCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }
    }
}

class ClientActivity : AppCompatActivity() {
    fun setupMicrophone() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        }
    }
}

// ✅ GOOD - Extract common logic
object PermissionUtils {
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == 
               PackageManager.PERMISSION_GRANTED
    }
    
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { hasPermission(context, it) }
    }
}

// Reusable base class for activities that need permissions
abstract class BasePermissionActivity : AppCompatActivity() {
    protected abstract fun getRequiredPermissions(): Array<String>
    
    protected fun checkAndRequestPermissions() {
        val permissions = getRequiredPermissions()
        if (!PermissionUtils.hasPermissions(this, permissions)) {
            permissionLauncher.launch(permissions)
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        onPermissionsResult(result)
    }
    
    protected abstract fun onPermissionsResult(result: Map<String, Boolean>)
}
```

**Apply DRY to**:
- **Permission handling**: Create a reusable `PermissionHandler` class
- **WebRTC setup**: Extract common initialization to base class
- **UI updates**: Create extension functions for common UI operations
- **Error handling**: Create centralized error handler
- **Signaling messages**: Create data classes for message types

```kotlin
// ✅ Reusable WebRTC base functionality
abstract class BaseWebRTCActivity : BasePermissionActivity() {
    protected lateinit var webRTCClient: IWebRTCClient
    protected lateinit var signalingClient: ISignalingClient
    
    protected fun initializeWebRTC() {
        webRTCClient = createWebRTCClient()
        signalingClient = createSignalingClient()
        signalingClient.connect()
    }
    
    protected abstract fun createWebRTCClient(): IWebRTCClient
    protected abstract fun createSignalingClient(): ISignalingClient
    
    override fun onDestroy() {
        super.onDestroy()
        webRTCClient.close()
        signalingClient.disconnect()
    }
}
```

---

### KISS (Keep It Simple, Stupid)

Write simple, straightforward code that is easy to understand and maintain.

**Examples**:

```kotlin
// ❌ BAD - Overly complex
class ConnectionManager {
    fun handleConnectionState(state: Int, isServer: Boolean, hasClient: Boolean, 
                             timestamp: Long, retryCount: Int, maxRetries: Int) {
        if ((state == 1 && isServer && hasClient) || (state == 2 && !isServer)) {
            if (timestamp > 0 && retryCount < maxRetries) {
                // Complex nested logic
            }
        }
    }
}

// ✅ GOOD - Simple and clear
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val peerId: String) : ConnectionState()
    data class Failed(val reason: String) : ConnectionState()
}

class ConnectionManager {
    fun handleConnectionState(state: ConnectionState) {
        when (state) {
            is ConnectionState.Disconnected -> handleDisconnected()
            is ConnectionState.Connecting -> handleConnecting()
            is ConnectionState.Connected -> handleConnected(state.peerId)
            is ConnectionState.Failed -> handleFailed(state.reason)
        }
    }
    
    private fun handleDisconnected() { /* Clear logic */ }
    private fun handleConnecting() { /* Clear logic */ }
    private fun handleConnected(peerId: String) { /* Clear logic */ }
    private fun handleFailed(reason: String) { /* Clear logic */ }
}
```

**KISS Guidelines**:
- Use descriptive variable and function names
- Keep functions small and focused (max 20-30 lines)
- Prefer composition over complex inheritance hierarchies
- Use sealed classes for state management
- Avoid premature optimization
- Use Kotlin's language features (data classes, sealed classes, extension functions)

```kotlin
// ✅ Simple state management
sealed class StreamingState {
    object Idle : StreamingState()
    object Initializing : StreamingState()
    data class Streaming(val roomCode: String, val clientCount: Int) : StreamingState()
    object Stopped : StreamingState()
}

class StreamingViewModel : ViewModel() {
    private val _state = MutableLiveData<StreamingState>(StreamingState.Idle)
    val state: LiveData<StreamingState> = _state
    
    fun startStreaming(roomCode: String) {
        _state.value = StreamingState.Initializing
        // Simple, linear flow
    }
    
    fun onClientConnected() {
        val current = _state.value
        if (current is StreamingState.Streaming) {
            _state.value = current.copy(clientCount = current.clientCount + 1)
        }
    }
}
```

---

### Design Patterns for This Project

#### 1. **Observer Pattern** (Already in WebRTC)
Used for WebRTC callbacks and signaling events.

```kotlin
// Implement observer pattern for signaling events
interface SignalingEventListener {
    fun onRoomCreated(roomId: String)
    fun onClientJoined(clientId: String)
    fun onOfferReceived(offer: SessionDescription)
    fun onConnectionError(error: String)
}

class SignalingClient(private val serverUrl: String) {
    private val listeners = mutableListOf<SignalingEventListener>()
    
    fun addListener(listener: SignalingEventListener) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: SignalingEventListener) {
        listeners.remove(listener)
    }
    
    private fun notifyRoomCreated(roomId: String) {
        listeners.forEach { it.onRoomCreated(roomId) }
    }
}
```

#### 2. **Factory Pattern**
Create complex objects without exposing instantiation logic.

```kotlin
// Factory for creating WebRTC components
class WebRTCFactory(private val context: Context) {
    
    fun createPeerConnectionFactory(): PeerConnectionFactory {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        
        PeerConnectionFactory.initialize(options)
        
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(createVideoEncoderFactory())
            .setVideoDecoderFactory(createVideoDecoderFactory())
            .createPeerConnectionFactory()
    }
    
    fun createPeerConnection(
        factory: PeerConnectionFactory,
        observer: PeerConnection.Observer
    ): PeerConnection? {
        val iceServers = Constants.ICE_SERVERS.map { url ->
            PeerConnection.IceServer.builder(url).createIceServer()
        }
        
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
        }
        
        return factory.createPeerConnection(rtcConfig, observer)
    }
    
    private fun createVideoEncoderFactory(): VideoEncoderFactory {
        return DefaultVideoEncoderFactory(
            EglBase.create().eglBaseContext,
            true,
            true
        )
    }
    
    private fun createVideoDecoderFactory(): VideoDecoderFactory {
        return DefaultVideoDecoderFactory(EglBase.create().eglBaseContext)
    }
}

// Usage
val factory = WebRTCFactory(context)
val peerConnectionFactory = factory.createPeerConnectionFactory()
val peerConnection = factory.createPeerConnection(peerConnectionFactory, observer)
```

#### 3. **Builder Pattern**
Construct complex objects step by step.

```kotlin
// Builder for WebRTC client configuration
class WebRTCClientConfig private constructor(
    val iceServers: List<String>,
    val videoWidth: Int,
    val videoHeight: Int,
    val videoFps: Int,
    val enableAudio: Boolean,
    val enableVideo: Boolean
) {
    class Builder {
        private var iceServers: List<String> = Constants.ICE_SERVERS
        private var videoWidth: Int = 1280
        private var videoHeight: Int = 720
        private var videoFps: Int = 30
        private var enableAudio: Boolean = true
        private var enableVideo: Boolean = true
        
        fun iceServers(servers: List<String>) = apply { this.iceServers = servers }
        fun videoResolution(width: Int, height: Int) = apply { 
            this.videoWidth = width
            this.videoHeight = height
        }
        fun videoFps(fps: Int) = apply { this.videoFps = fps }
        fun enableAudio(enable: Boolean) = apply { this.enableAudio = enable }
        fun enableVideo(enable: Boolean) = apply { this.enableVideo = enable }
        
        fun build() = WebRTCClientConfig(
            iceServers, videoWidth, videoHeight, videoFps, enableAudio, enableVideo
        )
    }
}

// Usage
val config = WebRTCClientConfig.Builder()
    .videoResolution(1920, 1080)
    .videoFps(30)
    .enableAudio(true)
    .build()
```

#### 4. **Strategy Pattern**
Define family of algorithms and make them interchangeable.

```kotlin
// Strategy for different video quality settings
interface VideoQualityStrategy {
    fun getWidth(): Int
    fun getHeight(): Int
    fun getFps(): Int
    fun getBitrate(): Int
}

class HighQualityStrategy : VideoQualityStrategy {
    override fun getWidth() = 1920
    override fun getHeight() = 1080
    override fun getFps() = 30
    override fun getBitrate() = 2500000
}

class MediumQualityStrategy : VideoQualityStrategy {
    override fun getWidth() = 1280
    override fun getHeight() = 720
    override fun getFps() = 30
    override fun getBitrate() = 1500000
}

class LowQualityStrategy : VideoQualityStrategy {
    override fun getWidth() = 640
    override fun getHeight() = 480
    override fun getFps() = 24
    override fun getBitrate() = 800000
}

class VideoQualityManager {
    private var strategy: VideoQualityStrategy = MediumQualityStrategy()
    
    fun setQualityStrategy(strategy: VideoQualityStrategy) {
        this.strategy = strategy
    }
    
    fun applyQuality(videoSource: VideoSource) {
        videoSource.adaptOutputFormat(
            strategy.getWidth(),
            strategy.getHeight(),
            strategy.getFps()
        )
    }
}

// Usage - easily switch quality based on network conditions
val qualityManager = VideoQualityManager()
if (networkSpeed > 5_000_000) {
    qualityManager.setQualityStrategy(HighQualityStrategy())
} else {
    qualityManager.setQualityStrategy(LowQualityStrategy())
}
```

#### 5. **Repository Pattern**
Abstract data access layer.

```kotlin
// Repository for room code storage
interface RoomCodeRepository {
    suspend fun saveRoomCode(roomCode: String)
    suspend fun getRecentRoomCodes(): List<String>
    suspend fun clearRoomCodes()
}

class RoomCodeRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : RoomCodeRepository {
    
    override suspend fun saveRoomCode(roomCode: String) = withContext(Dispatchers.IO) {
        val codes = getRecentRoomCodes().toMutableList()
        codes.remove(roomCode)  // Remove if exists
        codes.add(0, roomCode)  // Add to front
        val limitedCodes = codes.take(10)  // Keep only last 10
        
        sharedPreferences.edit()
            .putString(KEY_ROOM_CODES, gson.toJson(limitedCodes))
            .apply()
    }
    
    override suspend fun getRecentRoomCodes(): List<String> = withContext(Dispatchers.IO) {
        val json = sharedPreferences.getString(KEY_ROOM_CODES, null)
        if (json != null) {
            gson.fromJson(json, Array<String>::class.java).toList()
        } else {
            emptyList()
        }
    }
    
    override suspend fun clearRoomCodes() = withContext(Dispatchers.IO) {
        sharedPreferences.edit().remove(KEY_ROOM_CODES).apply()
    }
    
    companion object {
        private const val KEY_ROOM_CODES = "recent_room_codes"
    }
}
```

#### 6. **State Pattern**
Manage object state transitions.

```kotlin
// State pattern for streaming lifecycle
sealed class StreamingLifecycleState {
    abstract fun start(context: StreamingContext)
    abstract fun stop(context: StreamingContext)
    abstract fun pause(context: StreamingContext)
    
    object Idle : StreamingLifecycleState() {
        override fun start(context: StreamingContext) {
            context.initializeWebRTC()
            context.setState(Initializing)
        }
        override fun stop(context: StreamingContext) { /* Already stopped */ }
        override fun pause(context: StreamingContext) { /* Cannot pause from idle */ }
    }
    
    object Initializing : StreamingLifecycleState() {
        override fun start(context: StreamingContext) { /* Already starting */ }
        override fun stop(context: StreamingContext) {
            context.cleanup()
            context.setState(Idle)
        }
        override fun pause(context: StreamingContext) { /* Cannot pause while initializing */ }
    }
    
    object Active : StreamingLifecycleState() {
        override fun start(context: StreamingContext) { /* Already active */ }
        override fun stop(context: StreamingContext) {
            context.stopStreaming()
            context.cleanup()
            context.setState(Idle)
        }
        override fun pause(context: StreamingContext) {
            context.pauseStreaming()
            context.setState(Paused)
        }
    }
    
    object Paused : StreamingLifecycleState() {
        override fun start(context: StreamingContext) {
            context.resumeStreaming()
            context.setState(Active)
        }
        override fun stop(context: StreamingContext) {
            context.stopStreaming()
            context.cleanup()
            context.setState(Idle)
        }
        override fun pause(context: StreamingContext) { /* Already paused */ }
    }
}

class StreamingContext {
    private var state: StreamingLifecycleState = StreamingLifecycleState.Idle
    
    fun start() = state.start(this)
    fun stop() = state.stop(this)
    fun pause() = state.pause(this)
    
    internal fun setState(newState: StreamingLifecycleState) {
        state = newState
    }
    
    internal fun initializeWebRTC() { /* ... */ }
    internal fun stopStreaming() { /* ... */ }
    internal fun pauseStreaming() { /* ... */ }
    internal fun resumeStreaming() { /* ... */ }
    internal fun cleanup() { /* ... */ }
}
```

#### 7. **Singleton Pattern**
Ensure a class has only one instance (use with caution in Android).

```kotlin
// Use Kotlin object for singletons
object WebRTCManager {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    
    fun initialize(context: Context) {
        if (peerConnectionFactory == null) {
            val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)
            
            peerConnectionFactory = PeerConnectionFactory.builder()
                .createPeerConnectionFactory()
        }
    }
    
    fun getPeerConnectionFactory(): PeerConnectionFactory {
        return peerConnectionFactory 
            ?: throw IllegalStateException("WebRTCManager not initialized")
    }
}

// Usage
WebRTCManager.initialize(applicationContext)
val factory = WebRTCManager.getPeerConnectionFactory()
```

#### 8. **Dependency Injection Pattern** (Recommended)
While you can implement manual DI, consider using Dagger/Hilt for production apps:

```kotlin
// Manual dependency injection
class AppDependencies(private val context: Context) {
    
    val webRTCFactory: WebRTCFactory by lazy {
        WebRTCFactory(context)
    }
    
    val signalingClientFactory: SignalingClientFactory by lazy {
        SignalingClientFactory(Constants.SIGNALING_SERVER_URL)
    }
    
    val roomCodeRepository: RoomCodeRepository by lazy {
        RoomCodeRepositoryImpl(
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE),
            Gson()
        )
    }
}

// Application class
class SecurityCamApplication : Application() {
    lateinit var dependencies: AppDependencies
    
    override fun onCreate() {
        super.onCreate()
        dependencies = AppDependencies(this)
    }
}

// Usage in Activity
class ServerActivity : AppCompatActivity() {
    private val dependencies by lazy {
        (application as SecurityCamApplication).dependencies
    }
    
    private lateinit var webRTCClient: WebRTCClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val factory = dependencies.webRTCFactory
        webRTCClient = factory.createWebRTCClient()
    }
}
```

---

### Summary: Best Practices Checklist

When implementing this project, ensure:

**SOLID Compliance**:
- [ ] Each class has a single, well-defined responsibility
- [ ] Use interfaces to allow extension without modification
- [ ] Subtypes can replace base types without breaking functionality
- [ ] Split large interfaces into smaller, focused ones
- [ ] Depend on abstractions, inject dependencies

**DRY Compliance**:
- [ ] Extract common code into reusable utilities
- [ ] Create base classes for shared functionality
- [ ] Use extension functions for repeated operations
- [ ] Avoid copy-pasting code

**KISS Compliance**:
- [ ] Write simple, readable code
- [ ] Use descriptive names
- [ ] Keep functions small and focused
- [ ] Prefer composition over complex inheritance
- [ ] Use Kotlin's expressive features

**Design Patterns**:
- [ ] Observer pattern for callbacks and events
- [ ] Factory pattern for complex object creation
- [ ] Builder pattern for configurable objects
- [ ] Strategy pattern for interchangeable algorithms
- [ ] Repository pattern for data access
- [ ] State pattern for lifecycle management
- [ ] Singleton (object) for shared resources

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