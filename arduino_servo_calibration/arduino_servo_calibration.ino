/*
 * Servo Range Calibration Tool
 * Find the perfect pulse width range for your servo/finger setup
 */

#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>

Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver();

const int TEST_CHANNEL = 4;
const int SERVO_FREQ = 50;

// Test different ranges - UPDATED for 300-degree servos
int currentMinPulse = 400;   // Lower for 300-degree servos
int currentMaxPulse = 2200;  // Higher for 300-degree servos
int currentPulse = 1500;

void setup() {
  Serial.begin(9600);
  Serial.println(F("=== Servo Range Calibration ==="));
  
  // Initialize PCA9685
  Wire.begin();
  pwm.begin();
  pwm.setOscillatorFrequency(27000000);
  pwm.setPWMFreq(SERVO_FREQ);
  delay(10);
  
  Serial.println(F("Commands:"));
  Serial.println(F("min    - Move to minimum (closed)"));
  Serial.println(F("max    - Move to maximum (open)"));
  Serial.println(F("mid    - Move to middle"));
  Serial.println(F("XXX    - Move to specific pulse (e.g., 1200)"));
  Serial.println(F("range  - Show current range"));
  Serial.println(F("setmin XXX - Set new minimum pulse"));
  Serial.println(F("setmax XXX - Set new maximum pulse"));
  Serial.println(F("test   - Test current range"));
  Serial.println();
  
  moveServo(1500);
  Serial.println(F("Ready for calibration!"));
}

void loop() {
  if (Serial.available()) {
    String input = Serial.readString();
    input.trim();
    input.toLowerCase();
    
    if (input == "min") {
      moveServo(currentMinPulse);
      Serial.print(F("MIN: "));
      Serial.println(currentMinPulse);
      
    } else if (input == "max") {
      moveServo(currentMaxPulse);
      Serial.print(F("MAX: "));
      Serial.println(currentMaxPulse);
      
    } else if (input == "mid") {
      int midPulse = (currentMinPulse + currentMaxPulse) / 2;
      moveServo(midPulse);
      Serial.print(F("MID: "));
      Serial.println(midPulse);
      
    } else if (input == "range") {
      Serial.print(F("Current range: "));
      Serial.print(currentMinPulse);
      Serial.print(F(" - "));
      Serial.println(currentMaxPulse);
      
    } else if (input.startsWith("setmin ")) {
      int newMin = input.substring(7).toInt();
      if (newMin >= 300 && newMin <= 1500) {  // Wider range for 300° servos
        currentMinPulse = newMin;
        Serial.print(F("New MIN set to: "));
        Serial.println(currentMinPulse);
        moveServo(currentMinPulse);
      } else {
        Serial.println(F("Invalid range (300-1500)"));
      }
      
    } else if (input.startsWith("setmax ")) {
      int newMax = input.substring(7).toInt();
      if (newMax >= 1500 && newMax <= 3000) {  // Wider range for 300° servos
        currentMaxPulse = newMax;
        Serial.print(F("New MAX set to: "));
        Serial.println(currentMaxPulse);
        moveServo(currentMaxPulse);
      } else {
        Serial.println(F("Invalid range (1500-3000)"));
      }
      
    } else if (input == "test") {
      testRange();
      
    } else {
      // Try to parse as pulse width
      int pulse = input.toInt();
      if (pulse >= 300 && pulse <= 3000) {  // Wider range for 300° servos
        moveServo(pulse);
        Serial.print(F("Moved to: "));
        Serial.println(pulse);
      } else {
        Serial.println(F("Unknown command or invalid pulse"));
      }
    }
  }
  delay(100);
}

void moveServo(int pulse) {
  long pwmVal = ((long)pulse * 4096L) / 20000L;
  pwmVal = constrain(pwmVal, 0, 4095);
  
  pwm.setPWM(TEST_CHANNEL, 0, (int)pwmVal);
  
  Serial.print(F("Pulse: "));
  Serial.print(pulse);
  Serial.print(F("us PWM: "));
  Serial.println((int)pwmVal);
}

void testRange() {
  Serial.println(F("=== Testing Current Range ==="));
  
  Serial.println(F("Moving to CLOSED position..."));
  moveServo(currentMinPulse);
  delay(2000);
  
  Serial.println(F("Moving to OPEN position..."));
  moveServo(currentMaxPulse);
  delay(2000);
  
  Serial.println(F("Moving to MIDDLE position..."));
  int midPulse = (currentMinPulse + currentMaxPulse) / 2;
  moveServo(midPulse);
  delay(1000);
  
  Serial.println(F("=== Test Complete ==="));
  Serial.print(F("Current range: "));
  Serial.print(currentMinPulse);
  Serial.print(F(" (closed) to "));
  Serial.print(currentMaxPulse);
  Serial.println(F(" (open)"));
  Serial.println();
  Serial.println(F("If finger doesn't close enough:"));
  Serial.println(F("  Try: setmin 300  (or lower for 300° servos)"));
  Serial.println(F("If finger doesn't open enough:"));
  Serial.println(F("  Try: setmax 2800 (or higher for 300° servos)"));
  Serial.println(F("Note: 300-degree servos need wider pulse ranges!"));
}
