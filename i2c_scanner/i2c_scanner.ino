/*
 * I2C Scanner - Finds all I2C devices
 * This will help determine if the issue is:
 * - Arduino I2C pins not working
 * - PCA9685 board defective
 * - Wrong I2C address
 * - Wiring issue
 */

#include <Wire.h>

void setup() {
  Wire.begin();
  Serial.begin(9600);
  while (!Serial);
  
  Serial.println(F("I2C Scanner Starting..."));
  Serial.println(F("Scanning for I2C devices..."));
}

void loop() {
  byte error, address;
  int nDevices = 0;

  Serial.println(F("Scanning..."));

  for(address = 1; address < 127; address++) {
    Wire.beginTransmission(address);
    error = Wire.endTransmission();

    if (error == 0) {
      Serial.print(F("I2C device found at address 0x"));
      if (address < 16) Serial.print(F("0"));
      Serial.print(address, HEX);
      Serial.println(F(" !"));
      nDevices++;
    }
    else if (error == 4) {
      Serial.print(F("Unknown error at address 0x"));
      if (address < 16) Serial.print(F("0"));
      Serial.println(address, HEX);
    }    
  }
  
  if (nDevices == 0) {
    Serial.println(F("No I2C devices found"));
    Serial.println(F("Check wiring:"));
    Serial.println(F("- A4 -> SDA"));
    Serial.println(F("- A5 -> SCL"));
    Serial.println(F("- 5V -> VCC"));
    Serial.println(F("- GND -> GND"));
  } else {
    Serial.print(nDevices);
    Serial.println(F(" device(s) found"));
  }

  delay(5000);
}
