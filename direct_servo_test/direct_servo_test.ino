/*
 * Direct Servo Test - Bypass PCA9685
 * Connect servo directly to Arduino to test if servo works
 * 
 * Wiring:
 * Servo Red -> Arduino 5V (or external 5V)
 * Servo Black/Brown -> GND
 * Servo Orange/Yellow -> Pin 9
 */

#include <Servo.h>

Servo testServo;

void setup() {
  Serial.begin(9600);
  Serial.println(F("Direct Servo Test"));
  
  testServo.attach(9);
  Serial.println(F("Servo attached to pin 9"));
  Serial.println(F("Commands: 0, 90, 180, sweep"));
}

void loop() {
  if (Serial.available()) {
    String cmd = Serial.readString();
    cmd.trim();
    
    if (cmd == "0") {
      testServo.write(0);
      Serial.println(F("Moved to 0 degrees"));
    } else if (cmd == "90") {
      testServo.write(90);
      Serial.println(F("Moved to 90 degrees"));
    } else if (cmd == "180") {
      testServo.write(180);
      Serial.println(F("Moved to 180 degrees"));
    } else if (cmd == "sweep") {
      Serial.println(F("Sweeping..."));
      for (int i = 0; i <= 180; i += 10) {
        testServo.write(i);
        delay(100);
      }
      for (int i = 180; i >= 0; i -= 10) {
        testServo.write(i);
        delay(100);
      }
      Serial.println(F("Sweep complete"));
    }
  }
  delay(100);
}
