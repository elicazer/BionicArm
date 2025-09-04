# Bionic Arm Setup Guide

## Overview
Your BionicArm app now includes USB serial communication to control an Arduino-based bionic arm. The app calculates finger positions as percentages (0-100%) and sends them to your Arduino via USB.

## What's New

### Android App Features:
- **Real-time finger tracking** with MediaPipe hand landmarks
- **Finger position calculation** as percentages (0% = closed, 100% = open)
- **USB serial communication** to Arduino
- **Connection status display** with visual indicators
- **Live finger position display** in the UI
- **Automatic Arduino detection** and connection

### Data Format Sent to Arduino:
```
T:XX,I:XX,M:XX,R:XX,P:XX\n
```
Where:
- T = Thumb (0-100%)
- I = Index finger (0-100%)
- M = Middle finger (0-100%)
- R = Ring finger (0-100%)
- P = Pinky (0-100%)

## Hardware Setup

### Required Components:
1. **Arduino Uno** (or compatible)
2. **5 Servo Motors** (SG90 or similar)
3. **USB OTG Cable** (to connect phone to Arduino)
4. **Jumper wires**
5. **Breadboard** (optional)
6. **External power supply** (recommended for servos)

### Arduino Wiring:
```
Servo Connections:
- Thumb Servo  → Pin 3
- Index Servo  → Pin 5
- Middle Servo → Pin 6
- Ring Servo   → Pin 9
- Pinky Servo  → Pin 10

Power:
- Servo VCC → 5V (external power recommended)
- Servo GND → GND
- Arduino GND → External power GND
```

### Arduino Code:
Upload the provided `arduino_bionic_arm.ino` file to your Arduino Uno. This code:
- Receives finger position data via USB serial
- Controls 5 servo motors
- Provides debug output via Serial Monitor
- Includes servo testing functions

## Software Setup

### Android App Usage:
1. **Launch the app** - Grant camera permission
2. **Connect Arduino** - Tap "Connect Arduino" button
3. **Grant USB permission** - Allow access when prompted
4. **Start tracking** - Show your hand to the camera
5. **Watch the magic** - Servos will mimic your finger movements!

### Status Indicators:
- **Green checkmark** = Arduino connected and ready
- **Orange warning** = Arduino not connected
- **Real-time percentages** = Current finger positions

## Troubleshooting

### Arduino Connection Issues:
- Ensure USB OTG cable is working
- Check Arduino is powered on
- Try different USB ports
- Verify Arduino code is uploaded

### Servo Issues:
- Check servo wiring and power supply
- Adjust `MIN_ANGLE` and `MAX_ANGLE` in Arduino code
- Test servos individually using Serial Monitor

### Hand Tracking Issues:
- Ensure good lighting conditions
- Keep hand clearly visible in camera frame
- Try switching between front/back cameras

## Customization

### Servo Calibration:
Edit these values in the Arduino code:
```cpp
const int MIN_ANGLE = 0;   // Fully closed position
const int MAX_ANGLE = 180; // Fully open position
```

### Finger Sensitivity:
Adjust the finger position calculation in `FingerPositionCalculator.kt` if needed.

### Serial Communication:
- Baud rate: 9600 (configurable in both Android and Arduino code)
- Data format is easily customizable

## Advanced Features

### Testing Servos:
Use Arduino Serial Monitor and send "test" command to run servo tests.

### Multiple Hands:
The app can detect up to 2 hands but currently uses only the first detected hand.

### Custom Gestures:
You can extend the finger position calculator to detect specific gestures and send custom commands.

## Safety Notes

- **Power Supply**: Use external power for servos to avoid damaging Arduino
- **Current Limits**: Don't exceed Arduino's current capacity
- **Mechanical Limits**: Set appropriate servo angle limits to prevent damage
- **Testing**: Always test individual components before full assembly

## Next Steps

1. **Upload Arduino code** to your Arduino Uno
2. **Wire the servos** according to the pin diagram
3. **Connect Arduino to phone** via USB OTG
4. **Launch the app** and test the connection
5. **Calibrate servos** for your specific bionic arm setup

Your bionic arm should now respond to your hand movements in real-time! 🦾

## Support

If you encounter issues:
1. Check the console logs in Android Studio
2. Use Arduino Serial Monitor for debugging
3. Verify all connections and power supplies
4. Test components individually before integration
