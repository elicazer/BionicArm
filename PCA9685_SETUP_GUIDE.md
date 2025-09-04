# PCA9685 Bionic Arm Setup Guide

## Overview
This setup uses the PCA9685 16-channel PWM driver to control 5 servo motors for your bionic arm. The PCA9685 provides precise servo control and can handle the power requirements better than direct Arduino control.

## Required Components

### Hardware:
- **Arduino Uno** (or compatible)
- **PCA9685 16-Channel PWM Driver Board**
- **5 Servo Motors** (SG90, MG996R, or similar)
- **USB OTG Cable** (phone to Arduino)
- **Jumper Wires**
- **External Power Supply** (5V 3-5A recommended for servos)
- **Breadboard** (optional)

### Software Libraries:
- **Adafruit PWM Servo Driver Library**
- **Wire Library** (included with Arduino IDE)

## Wiring Connections

### Arduino Uno ↔ PCA9685:
```
Arduino Uno    PCA9685
-----------    -------
5V       →     VCC
GND      →     GND
A4 (SDA) →     SDA
A5 (SCL) →     SCL
```

### PCA9685 ↔ Servos:
```
PCA9685 Channel    Servo        Finger
---------------    -----        ------
Channel 0      →   Servo 1  →   Thumb
Channel 1      →   Servo 2  →   Index
Channel 2      →   Servo 3  →   Middle
Channel 3      →   Servo 4  →   Ring
Channel 4      →   Servo 5  →   Pinky
```

### Power Connections:
```
External 5V Power Supply:
- Positive → PCA9685 V+ terminal
- Negative → PCA9685 GND terminal
- Negative → Arduino GND (common ground)

Servo Power:
- Each servo Red wire → PCA9685 V+ (via terminal block)
- Each servo Brown/Black wire → PCA9685 GND
- Each servo Orange/Yellow wire → PCA9685 PWM channel
```

## Library Installation

### Install Adafruit PWM Servo Driver Library:
1. Open Arduino IDE
2. Go to **Sketch → Include Library → Manage Libraries**
3. Search for "Adafruit PWM Servo Driver"
4. Install the library by Adafruit
5. Also install "Adafruit Bus IO" if prompted

### Alternative Installation:
```bash
# Via Arduino CLI
arduino-cli lib install "Adafruit PWM Servo Driver Library"
```

## Code Configuration

### Servo Pulse Width Calibration:
```cpp
// Adjust these values for your specific servos
const int SERVO_MIN_PULSE = 500;   // Minimum pulse (closed position)
const int SERVO_MAX_PULSE = 2500;  // Maximum pulse (open position)
```

### Common Servo Pulse Widths:
- **SG90 Micro Servo**: 500-2400 µs
- **MG996R Standard Servo**: 1000-2000 µs
- **Generic Servo**: 1000-2000 µs

### Smoothing Configuration:
```cpp
const int SMOOTHING_FACTOR = 3; // Higher = more smoothing (1-10)
```

## Setup Steps

### 1. Hardware Assembly:
1. Connect Arduino to PCA9685 via I2C (SDA/SCL)
2. Connect external 5V power supply to PCA9685
3. Connect servos to PCA9685 channels 0-4
4. Ensure common ground between all components

### 2. Software Setup:
1. Install required libraries
2. Upload `arduino_pca9685_bionic_arm.ino` to Arduino
3. Open Serial Monitor (9600 baud)
4. Verify initialization messages

### 3. Testing:
```cpp
// Send these commands via Serial Monitor:
testServos()        // Test all servos
calibrateServo(0)   // Calibrate channel 0 (thumb)
emergencyStop()     // Stop all servos
```

## Features

### Advanced Servo Control:
- **Precise PWM Control**: 12-bit resolution (4096 steps)
- **Smooth Movement**: Built-in smoothing to reduce jitter
- **Individual Channel Control**: Each finger controlled independently
- **Calibration Functions**: Easy servo range adjustment

### Safety Features:
- **Emergency Stop**: Disable all servos instantly
- **Position Validation**: Input range checking (0-100%)
- **Change Threshold**: Prevents unnecessary servo updates
- **Power Management**: External power supply support

### Debugging Features:
- **Serial Output**: Real-time position feedback
- **PWM Value Display**: Shows calculated PWM values
- **Calibration Mode**: Test individual servo ranges
- **Smoothing Visualization**: Raw vs smoothed values

## Troubleshooting

### Common Issues:

#### Servos Not Moving:
- Check power supply (5V, sufficient amperage)
- Verify I2C connections (SDA/SCL)
- Confirm servo wiring to correct channels
- Test with `calibrateServo()` function

#### Jittery Movement:
- Increase `SMOOTHING_FACTOR` (try 5-8)
- Check power supply stability
- Adjust `threshold` in `positionsChanged()`

#### I2C Communication Errors:
- Verify SDA → A4, SCL → A5 connections
- Check for loose wires
- Ensure common ground
- Try different I2C address if needed

#### Servo Range Issues:
- Adjust `SERVO_MIN_PULSE` and `SERVO_MAX_PULSE`
- Use `calibrateServo()` to find optimal values
- Check servo specifications

### Serial Monitor Commands:
```
testServos()        - Run full servo test sequence
calibrateServo(0)   - Calibrate thumb servo (channel 0)
calibrateServo(1)   - Calibrate index servo (channel 1)
emergencyStop()     - Disable all servos immediately
```

## Performance Optimization

### Servo Response:
- **Update Rate**: 20ms (50Hz) for smooth movement
- **Smoothing**: Reduces jitter while maintaining responsiveness
- **Threshold**: Prevents micro-movements that waste power

### Power Efficiency:
- **External Power**: Dedicated 5V supply for servos
- **Selective Updates**: Only move servos when position changes
- **Emergency Stop**: Quick power-down capability

## Advanced Configuration

### Custom Servo Ranges:
```cpp
// For different servo types, adjust pulse widths:
// SG90: 500-2400µs
// MG996R: 1000-2000µs
// Custom: Measure your servo's actual range
```

### I2C Address Change:
```cpp
// If using multiple PCA9685 boards:
Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver(0x41);
```

### Frequency Adjustment:
```cpp
// For different servo requirements:
const int SERVO_FREQ = 50; // Standard servo frequency
// Some servos work better at 60Hz
```

Your bionic arm should now have precise, smooth servo control through the PCA9685! 🦾

## Next Steps:
1. Wire the PCA9685 according to the diagram
2. Install the Adafruit library
3. Upload the Arduino code
4. Test with Serial Monitor commands
5. Connect to your Android app and enjoy real-time control!
