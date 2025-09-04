# BionicArm - Hand Landmark Detection App

This Android app uses Google's MediaPipe library to detect and visualize hand landmarks in real-time using the device camera.

## Features

- Real-time hand landmark detection
- Camera preview with overlay visualization
- Switch between front and back cameras
- Supports detection of up to 2 hands simultaneously
- Hand skeleton visualization with connections between landmarks

## Setup Instructions

1. **Sync Project**: Open the project in Android Studio and sync Gradle files
2. **Model File**: The MediaPipe hand landmark model (`hand_landmarker.task`) should already be in `app/src/main/assets/`
3. **Permissions**: The app will request camera permission on first launch
4. **Build and Run**: Build and run the app on a physical device (camera required)

## Dependencies Added

- MediaPipe Tasks Vision: `com.google.mediapipe:tasks-vision:0.10.8`
- CameraX libraries for camera functionality
- Compose UI with ViewBinding support

## Key Components

- `MainActivity.kt`: Main activity with camera permission handling
- `HandLandmarkDetector.kt`: Main composable that orchestrates the detection
- `CameraPreview.kt`: Camera preview with MediaPipe integration
- `HandLandmarkOverlay.kt`: Custom view for drawing hand landmarks
- `ImageUtils.kt`: Utility functions for image processing

## Usage

1. Launch the app
2. Grant camera permission when prompted
3. Point the camera at your hand(s)
4. See real-time hand landmark detection with red dots and green connections
5. Use the floating action button to switch between front and back cameras

## Hand Landmark Model

The app uses MediaPipe's hand landmark model which detects 21 key points on each hand:
- Thumb: 5 points (0-4)
- Index finger: 4 points (5-8)
- Middle finger: 4 points (9-12)
- Ring finger: 4 points (13-16)
- Pinky: 4 points (17-20)

## Troubleshooting

- Ensure you're testing on a physical device (emulator cameras may not work properly)
- Make sure camera permission is granted
- Good lighting conditions improve detection accuracy
- Keep hands clearly visible in the camera frame
