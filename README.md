# BionicArm - AI-Powered Bionic Hand Controller

An Android app that uses Google's MediaPipe to detect hand landmarks in real-time and control a physical bionic arm via USB serial communication. Transform your hand movements into precise servo control for robotics projects.

## 🎥 Demo Videos

<div align="center">

### Live Hand Tracking Demo
[![Live Hand Tracking Demo](https://img.youtube.com/vi/FJIQqjLkAq0/maxresdefault.jpg)](https://youtube.com/shorts/FJIQqjLkAq0?si=dIcIpmy3Hb8ArGar)
*See real-time finger position detection*

### Bionic Arm Control Demo  
[![Bionic Arm Control Demo](https://img.youtube.com/vi/ELUgA3lYytk/maxresdefault.jpg)](https://youtube.com/shorts/ELUgA3lYytk?si=xfy5LaKfNbhFRPl7)
*Watch the bionic arm mimic hand movements*

</div>

## ✨ Features

### Android App:
- **Real-time hand landmark detection** using MediaPipe
- **Finger position calculation** as percentages (0-100%)
- **USB serial communication** to Arduino
- **Live connection status** with visual indicators
- **Camera switching** between front and back cameras
- **Automatic Arduino detection** and connection
- **Real-time finger position display** in UI
- **Screen stays on** during operation

### Hardware Control:
- **5-finger servo control** via Arduino
- **PCA9685 PWM driver support** for precise control
- **Smooth movement interpolation** to reduce jitter
- **Calibrated servo ranges** for optimal performance
- **Emergency stop functionality**
- **Connection timeout handling**

## 🚀 Quick Start

### Prerequisites
- Android device with camera
- Arduino Uno + 5 servo motors
- USB OTG cable
- PCA9685 PWM driver (recommended)

### Installation
1. **Clone the repository**
2. **Open in Android Studio** and sync Gradle files
3. **Upload Arduino code** from `arduino_inmoov_app_ready/` folder
4. **Wire servos** according to setup guides
5. **Build and run** the Android app
6. **Connect Arduino** via USB OTG cable

## 🔧 Hardware Setup

### Required Components:
- **Arduino Uno** (or compatible)
- **PCA9685 16-Channel PWM Driver**
- **5 Servo Motors** (SG90, MG996R, or similar)
- **USB OTG Cable**
- **External 5V Power Supply** (3-5A recommended)
- **Jumper wires and breadboard**

### Wiring:
```
Arduino → PCA9685:
5V → VCC, GND → GND
A4 → SDA, A5 → SCL

PCA9685 Channels:
Channel 0 → Thumb Servo
Channel 1 → Index Servo  
Channel 2 → Middle Servo
Channel 3 → Ring Servo
Channel 4 → Pinky Servo
```

For detailed wiring diagrams, see:
- [**BIONIC_ARM_SETUP.md**](BIONIC_ARM_SETUP.md) - Basic Arduino setup
- [**PCA9685_SETUP_GUIDE.md**](PCA9685_SETUP_GUIDE.md) - Advanced PCA9685 setup

## 📱 App Usage

1. **Launch the app** - Grant camera permission when prompted
2. **Connect Arduino** - Tap "Connect Arduino" button
3. **Grant USB permission** - Allow access to Arduino
4. **Show your hand** - Point camera at your hand
5. **Watch the magic** - Bionic arm mimics your movements!

### Status Indicators:
- 🟢 **Green checkmark** = Arduino connected and ready
- 🟠 **Orange warning** = Arduino not connected
- **Live percentages** = Current finger positions (0-100%)

## 🏗️ Project Structure

### Android App (`app/src/main/java/`):
- `MainActivity.kt` - Main activity with permissions
- `HandLandmarkDetector.kt` - Main detection composable
- `CameraPreview.kt` - Camera integration with MediaPipe
- `HandLandmarkOverlay.kt` - Hand visualization overlay
- `FingerPositionCalculator.kt` - Converts landmarks to percentages
- `UsbSerialManager.kt` - Arduino USB communication
- `ImageUtils.kt` - Image processing utilities

### Arduino Code:
- `arduino_inmoov_app_ready/` - Production-ready InMoov hand controller
- `arduino_servo_calibrated/` - Basic servo control
- `arduino_servo_calibration/` - Servo calibration tools
- `direct_servo_test/` - Individual servo testing
- `i2c_scanner/` - I2C device detection

## 🔧 Dependencies

### Android:
```kotlin
// MediaPipe for hand detection
implementation("com.google.mediapipe:tasks-vision:0.10.8")

// USB Serial communication
implementation("com.github.mik3y:usb-serial-for-android:3.7.3")

// Camera functionality
implementation("androidx.camera:camera-core:1.3.1")
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
```

### Arduino:
```cpp
#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>
```

## 📊 Data Protocol

The app sends finger positions to Arduino via USB serial:

**Format:** `T:XX,I:XX,M:XX,R:XX,P:XX\n`

Where:
- **T** = Thumb (0-100%)
- **I** = Index finger (0-100%)
- **M** = Middle finger (0-100%)
- **R** = Ring finger (0-100%)
- **P** = Pinky (0-100%)

**Example:** `T:45,I:78,M:82,R:65,P:23\n`

## 🎯 Hand Landmark Model

MediaPipe detects 21 key points per hand:
- **Thumb:** 5 points (landmarks 0-4)
- **Index finger:** 4 points (landmarks 5-8)
- **Middle finger:** 4 points (landmarks 9-12)
- **Ring finger:** 4 points (landmarks 13-16)
- **Pinky:** 4 points (landmarks 17-20)

## 🔧 Calibration

### Servo Calibration:
Edit ranges in Arduino code:
```cpp
ServoRange thumbRange = {400, 2200};  // Min/Max pulse width
ServoRange indexRange = {400, 2200};
// ... etc
```

### Finger Sensitivity:
Adjust mapping in `FingerPositionCalculator.kt`:
```kotlin
// Remap finger ranges based on your hand
val remappedIndex = map(indexPos, 50, 99, 0, 100)
```

## 🛠️ Troubleshooting

### Connection Issues:
- ✅ Check USB OTG cable functionality
- ✅ Verify Arduino is powered and code uploaded
- ✅ Grant USB permissions in Android
- ✅ Try different USB ports/cables

### Hand Tracking Issues:
- ✅ Ensure good lighting conditions
- ✅ Keep hand clearly visible in frame
- ✅ Try switching camera (front/back)
- ✅ Clean camera lens

### Servo Issues:
- ✅ Check servo wiring and power supply
- ✅ Verify PCA9685 I2C connections
- ✅ Test servos individually with calibration code
- ✅ Adjust pulse width ranges for your servos

## 🚀 Advanced Features

### Testing Commands:
Use Arduino Serial Monitor:
```
testServos()        - Test all servos
calibrateServo(0)   - Calibrate thumb servo
emergencyStop()     - Stop all servos
```

### Customization:
- **Multiple hand detection** (currently uses first detected hand)
- **Custom gesture recognition** (extend FingerPositionCalculator)
- **Different servo types** (adjust pulse width ranges)
- **Smoothing parameters** (modify SMOOTHING_FACTOR)

## 📄 Additional Documentation

- [**BIONIC_ARM_SETUP.md**](BIONIC_ARM_SETUP.md) - Complete hardware setup guide
- [**PCA9685_SETUP_GUIDE.md**](PCA9685_SETUP_GUIDE.md) - Advanced PWM driver setup
- [**WIRING_DIAGRAM.txt**](WIRING_DIAGRAM.txt) - Detailed wiring information

## ⚠️ Safety Notes

- **External Power:** Use dedicated 5V supply for servos
- **Current Limits:** Don't exceed Arduino's current capacity
- **Mechanical Limits:** Set appropriate servo angle limits
- **Testing:** Always test components individually first

## 🤝 Contributing

Contributions welcome! Please read the setup guides and test your changes with actual hardware.

## 📜 License

See [LICENSE](LICENSE) file for details.

---

**Ready to build your own bionic arm? Follow the setup guides and start controlling servos with your hand movements! 🦾**
