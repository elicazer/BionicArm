/*
 * PCA9685 Servo Test - CALIBRATED for Your 300° Servos
 * Optimized pulse range: 400-2200μs for proper finger closure/opening
 */

#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>

Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver();

const int TEST_CHANNEL = 0;
const int MIN_PULSE = 400;   // Your calibrated minimum (finger closed)
const int MAX_PULSE = 2200;  // Your calibrated maximum (finger open)
const int MID_PULSE = 1300;  // Middle position (400+2200)/2
const int SERVO_FREQ = 50;

void setup() {
  Serial.begin(9600);
  Serial.println(F("=== Calibrated Servo Controller ==="));
  
  // Initialize PCA9685
  Wire.begin();
  pwm.begin();
  pwm.setOscillatorFrequency(27000000);
  pwm.setPWMFreq(SERVO_FREQ);
  delay(10);
  
  Serial.println(F("Using calibrated range: 400-2200us"));
  Serial.println(F("Commands: min, max, mid, test, sweep"));
  Serial.println(F("Multi-servo: ch0, ch1, ch2, ch3, ch4"));
  Serial.println(F("All servos: all_min, all_max, all_mid"));
  
  // Start at middle position
  moveServo(TEST_CHANNEL, MID_PULSE);
  Serial.println(F("Ready for next level testing!"));
}

void loop() {
  if (Serial.available()) {
    String cmd = Serial.readString();
    cmd.trim();
    cmd.toLowerCase();
    
    // Single servo commands (channel 0)
    if (cmd == "min") {
      moveServo(TEST_CHANNEL, MIN_PULSE);
      Serial.println(F("CH0 MIN (closed)"));
    } else if (cmd == "max") {
      moveServo(TEST_CHANNEL, MAX_PULSE);
      Serial.println(F("CH0 MAX (open)"));
    } else if (cmd == "mid") {
      moveServo(TEST_CHANNEL, MID_PULSE);
      Serial.println(F("CH0 MID"));
    } else if (cmd == "test") {
      testSingleServo();
      
    // Multi-servo channel testing
    } else if (cmd == "ch0") {
      testChannel(0);
    } else if (cmd == "ch1") {
      testChannel(1);
    } else if (cmd == "ch2") {
      testChannel(2);
    } else if (cmd == "ch3") {
      testChannel(3);
    } else if (cmd == "ch4") {
      testChannel(4);
      
    // All servos commands
    } else if (cmd == "all_min") {
      moveAllServos(MIN_PULSE);
      Serial.println(F("ALL SERVOS MIN (closed fist)"));
    } else if (cmd == "all_max") {
      moveAllServos(MAX_PULSE);
      Serial.println(F("ALL SERVOS MAX (open hand)"));
    } else if (cmd == "all_mid") {
      moveAllServos(MID_PULSE);
      Serial.println(F("ALL SERVOS MID"));
    } else if (cmd == "all_test") {
      testAllServos();
      
    } else if (cmd == "sweep") {
      sweepServo(TEST_CHANNEL);
    } else if (cmd == "off") {
      pwm.setPWM(TEST_CHANNEL, 0, 0);
      Serial.println(F("CH0 OFF"));
    } else {
      Serial.println(F("Commands: min/max/mid/test/sweep/off"));
      Serial.println(F("Channels: ch0/ch1/ch2/ch3/ch4"));
      Serial.println(F("All: all_min/all_max/all_mid/all_test"));
    }
  }
  delay(100);
}

void moveServo(int channel, int pulse) {
  // Use your calibrated PWM calculation
  long pwmVal = ((long)pulse * 4096L) / 20000L;
  pwmVal = constrain(pwmVal, 0, 4095);
  
  pwm.setPWM(channel, 0, (int)pwmVal);
  
  Serial.print(F("Ch"));
  Serial.print(channel);
  Serial.print(F(": "));
  Serial.print(pulse);
  Serial.print(F("us PWM:"));
  Serial.println((int)pwmVal);
}

void testSingleServo() {
  Serial.println(F("=== Single Servo Test (Ch0) ==="));
  
  Serial.println(F("Closing finger..."));
  moveServo(0, MIN_PULSE);
  delay(1500);
  
  Serial.println(F("Opening finger..."));
  moveServo(0, MAX_PULSE);
  delay(1500);
  
  Serial.println(F("Middle position..."));
  moveServo(0, MID_PULSE);
  delay(1000);
  
  Serial.println(F("Single servo test complete!"));
}

void testChannel(int channel) {
  Serial.print(F("=== Testing Channel "));
  Serial.print(channel);
  Serial.println(F(" ==="));
  
  moveServo(channel, MID_PULSE);
  delay(500);
  moveServo(channel, MIN_PULSE);
  delay(1000);
  moveServo(channel, MAX_PULSE);
  delay(1000);
  moveServo(channel, MID_PULSE);
  delay(500);
  
  Serial.print(F("Channel "));
  Serial.print(channel);
  Serial.println(F(" test complete"));
}

void moveAllServos(int pulse) {
  Serial.print(F("Moving all servos to: "));
  Serial.println(pulse);
  
  for (int ch = 0; ch < 5; ch++) {
    moveServo(ch, pulse);
    delay(100); // Small delay between servos
  }
}

void testAllServos() {
  Serial.println(F("=== Testing All 5 Servos ==="));
  
  Serial.println(F("All fingers to middle..."));
  moveAllServos(MID_PULSE);
  delay(1000);
  
  Serial.println(F("Closing all fingers (fist)..."));
  moveAllServos(MIN_PULSE);
  delay(2000);
  
  Serial.println(F("Opening all fingers (hand)..."));
  moveAllServos(MAX_PULSE);
  delay(2000);
  
  Serial.println(F("Back to middle..."));
  moveAllServos(MID_PULSE);
  delay(1000);
  
  Serial.println(F("Testing individual fingers..."));
  for (int ch = 0; ch < 5; ch++) {
    String fingerNames[] = {"Thumb", "Index", "Middle", "Ring", "Pinky"};
    Serial.print(fingerNames[ch]);
    Serial.println(F(" finger test..."));
    
    moveServo(ch, MIN_PULSE);
    delay(800);
    moveServo(ch, MAX_PULSE);
    delay(800);
    moveServo(ch, MID_PULSE);
    delay(500);
  }
  
  Serial.println(F("=== All Servo Test Complete ==="));
  Serial.println(F("Ready for Android app integration!"));
}

void sweepServo(int channel) {
  Serial.print(F("Sweeping channel "));
  Serial.print(channel);
  Serial.println(F(" - send any key to stop"));
  
  while (!Serial.available()) {
    for (int p = MIN_PULSE; p <= MAX_PULSE && !Serial.available(); p += 50) {
      moveServo(channel, p);
      delay(100);
    }
    for (int p = MAX_PULSE; p >= MIN_PULSE && !Serial.available(); p -= 50) {
      moveServo(channel, p);
      delay(100);
    }
  }
  
  while (Serial.available()) Serial.read();
  Serial.println(F("Sweep stopped"));
}
