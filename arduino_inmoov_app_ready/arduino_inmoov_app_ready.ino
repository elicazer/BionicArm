/*
 * InMoov Hand Controller - Android App Ready
 * Calibrated for your 300° servos with 400-2200μs range
 * Receives finger data from Android app via USB Serial
 * 
 * Data format: "T:XX,I:XX,M:XX,R:XX,P:XX\n"
 * T=Thumb, I=Index, M=Middle, R=Ring, P=Pinky (0-100%)
 */

#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>

Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver();

// InMoov hand PCA9685 channel assignments
const int THUMB_CHANNEL = 0;
const int INDEX_CHANNEL = 1;
const int MIDDLE_CHANNEL = 2;
const int RING_CHANNEL = 3;
const int PINKY_CHANNEL = 4;

// Your calibrated servo ranges (400-2200μs)
struct ServoRange {
  int minPulse;  // Fully closed position
  int maxPulse;  // Fully open position
};

ServoRange thumbRange = {400, 2200};
ServoRange indexRange = {400, 2200};
ServoRange middleRange = {400, 2200};
ServoRange ringRange = {400, 2200};
ServoRange pinkyRange = {400, 2200};

const int SERVO_FREQ = 50;

// Serial data parsing
String inputString = "";
bool stringComplete = false;

struct FingerPositions {
  int thumb;
  int index;
  int middle;
  int ring;
  int pinky;
};

FingerPositions currentPositions = {0, 0, 0, 0, 0};
FingerPositions lastPositions = {-1, -1, -1, -1, -1};

// Smoothing for mechanical reliability
const int SMOOTHING_SAMPLES = 3;
int thumbHistory[3] = {0};
int indexHistory[3] = {0};
int middleHistory[3] = {0};
int ringHistory[3] = {0};
int pinkyHistory[3] = {0};
int historyIndex = 0;

// Movement timing
const int MOVEMENT_DELAY = 15;
unsigned long lastMoveTime = 0;

// Connection status
bool isConnected = false;
unsigned long lastDataTime = 0;
const unsigned long CONNECTION_TIMEOUT = 3000;

void setup() {
  Serial.begin(9600);
  
  // Initialize PCA9685
  pwm.begin();
  pwm.setOscillatorFrequency(27000000);
  pwm.setPWMFreq(SERVO_FREQ);
  delay(10);
  
  // Initialize to natural resting position (30% open)
  initializeInMoovHand();
  
  Serial.println(F("InMoov Hand Controller - Android App Ready"));
  Serial.println(F("Calibrated range: 400-2200us"));
  Serial.println(F("Channels: T=0, I=1, M=2, R=3, P=4"));
  Serial.println(F("Waiting for Android app connection..."));
  
  inputString.reserve(50);
}

void loop() {
  // Check for incoming serial data from Android app
  if (stringComplete) {
    parseFingerData(inputString);
    updateInMoovHand();
    
    // Mark as connected
    isConnected = true;
    lastDataTime = millis();
    
    // Clear for next input
    inputString = "";
    stringComplete = false;
  }
  
  // Check connection status
  checkConnectionStatus();
  
  delay(10);
}

void serialEvent() {
  while (Serial.available()) {
    char inChar = (char)Serial.read();
    
    if (inChar == '\n') {
      stringComplete = true;
    } else {
      inputString += inChar;
    }
  }
}

void parseFingerData(String data) {
  // Parse format: "T:XX,I:XX,M:XX,R:XX,P:XX"
  
  int thumbPos = extractValue(data, "T:");
  int indexPos = extractValue(data, "I:");
  int middlePos = extractValue(data, "M:");
  int ringPos = extractValue(data, "R:");
  int pinkyPos = extractValue(data, "P:");
  
  // Remap finger ranges to proper 0-100% based on your observations
  if (thumbPos >= 0) {
    // Thumb: 0-76% → 0-100%
    int remappedThumb = map(thumbPos, 0, 76, 0, 100);
    currentPositions.thumb = constrain(remappedThumb, 0, 100);
  }
  
  if (indexPos >= 0) {
    // Index: 50-99% → 0-100%
    int remappedIndex = map(indexPos, 50, 99, 0, 100);
    currentPositions.index = constrain(remappedIndex, 0, 100);
  }
  
  if (middlePos >= 0) {
    // Middle: 50-99% → 0-100%
    int remappedMiddle = map(middlePos, 50, 99, 0, 100);
    currentPositions.middle = constrain(remappedMiddle, 0, 100);
  }
  
  if (ringPos >= 0) {
    // Ring: 50-99% → 0-100%
    int remappedRing = map(ringPos, 50, 99, 0, 100);
    currentPositions.ring = constrain(remappedRing, 0, 100);
  }
  
  if (pinkyPos >= 0) {
    // Pinky: 50-99% → 0-100%
    int remappedPinky = map(pinkyPos, 50, 99, 0, 100);
    currentPositions.pinky = constrain(remappedPinky, 0, 100);
  }
  
  // Debug output - show both original and remapped values
  Serial.print(F("Raw: T:"));
  Serial.print(thumbPos);
  Serial.print(F(" I:"));
  Serial.print(indexPos);
  Serial.print(F(" M:"));
  Serial.print(middlePos);
  Serial.print(F(" R:"));
  Serial.print(ringPos);
  Serial.print(F(" P:"));
  Serial.print(pinkyPos);
  
  Serial.print(F(" | Mapped: T:"));
  Serial.print(currentPositions.thumb);
  Serial.print(F(" I:"));
  Serial.print(currentPositions.index);
  Serial.print(F(" M:"));
  Serial.print(currentPositions.middle);
  Serial.print(F(" R:"));
  Serial.print(currentPositions.ring);
  Serial.print(F(" P:"));
  Serial.println(currentPositions.pinky);
}

int extractValue(String data, String prefix) {
  int startIndex = data.indexOf(prefix);
  if (startIndex == -1) return -1;
  
  startIndex += prefix.length();
  int endIndex = data.indexOf(',', startIndex);
  if (endIndex == -1) endIndex = data.length();
  
  String valueStr = data.substring(startIndex, endIndex);
  return valueStr.toInt();
}

void updateInMoovHand() {
  if (positionsChanged() && (millis() - lastMoveTime >= MOVEMENT_DELAY)) {
    
    // Apply smoothing
    int smoothedThumb = applySmoothing(thumbHistory, currentPositions.thumb);
    int smoothedIndex = applySmoothing(indexHistory, currentPositions.index);
    int smoothedMiddle = applySmoothing(middleHistory, currentPositions.middle);
    int smoothedRing = applySmoothing(ringHistory, currentPositions.ring);
    int smoothedPinky = applySmoothing(pinkyHistory, currentPositions.pinky);
    
    // Move servos with calibrated ranges
    moveInMoovFinger(THUMB_CHANNEL, smoothedThumb, thumbRange);
    moveInMoovFinger(INDEX_CHANNEL, smoothedIndex, indexRange);
    moveInMoovFinger(MIDDLE_CHANNEL, smoothedMiddle, middleRange);
    moveInMoovFinger(RING_CHANNEL, smoothedRing, ringRange);
    moveInMoovFinger(PINKY_CHANNEL, smoothedPinky, pinkyRange);
    
    // Update history
    historyIndex = (historyIndex + 1) % SMOOTHING_SAMPLES;
    lastPositions = currentPositions;
    lastMoveTime = millis();
  }
}

bool positionsChanged() {
  const int threshold = 2;
  
  return (abs(currentPositions.thumb - lastPositions.thumb) > threshold ||
          abs(currentPositions.index - lastPositions.index) > threshold ||
          abs(currentPositions.middle - lastPositions.middle) > threshold ||
          abs(currentPositions.ring - lastPositions.ring) > threshold ||
          abs(currentPositions.pinky - lastPositions.pinky) > threshold);
}

int applySmoothing(int history[], int newValue) {
  history[historyIndex] = newValue;
  
  int sum = 0;
  for (int i = 0; i < SMOOTHING_SAMPLES; i++) {
    sum += history[i];
  }
  
  return sum / SMOOTHING_SAMPLES;
}

void moveInMoovFinger(int channel, int percentage, ServoRange range) {
  // Convert percentage (0-100) to your calibrated pulse width
  int pulseWidth = map(percentage, 0, 100, range.minPulse, range.maxPulse);
  
  // Convert to PWM value using your working calculation
  long pwmValue = ((long)pulseWidth * 4096L) / 20000L;
  pwmValue = constrain(pwmValue, 0, 4095);
  
  // Set PWM
  pwm.setPWM(channel, 0, (int)pwmValue);
}

void initializeInMoovHand() {
  Serial.println(F("Initializing InMoov hand..."));
  
  // Set to 30% open (natural resting position)
  for (int ch = 0; ch < 5; ch++) {
    ServoRange* ranges[] = {&thumbRange, &indexRange, &middleRange, &ringRange, &pinkyRange};
    moveInMoovFinger(ch, 30, *ranges[ch]);
    delay(200);
  }
  
  Serial.println(F("InMoov hand ready"));
}

void checkConnectionStatus() {
  if (isConnected && (millis() - lastDataTime > CONNECTION_TIMEOUT)) {
    isConnected = false;
    Serial.println(F("Android app disconnected"));
    
    // Return to resting position when disconnected
    for (int ch = 0; ch < 5; ch++) {
      ServoRange* ranges[] = {&thumbRange, &indexRange, &middleRange, &ringRange, &pinkyRange};
      moveInMoovFinger(ch, 30, *ranges[ch]);
    }
  }
}
