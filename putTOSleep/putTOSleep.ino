/*
  AnalogReadSerial

  Reads an analog input on pin 0, prints the result to the Serial Monitor.
  Graphical representation is available using Serial Plotter (Tools > Serial Plotter menu).
  Attach the center pin of a potentiometer to pin A0, and the outside pins to +5V and ground.

  This example code is in the public domain.

  http://www.arduino.cc/en/Tutorial/AnalogReadSerial
*/

// the setup routine runs once when you press reset:

#include <FastLED.h>
#include <Arduino.h>
#include <analogWrite.h>
#include <Wire.h>

#define LED_PIN     15
#define NUM_LEDS    10

CRGB leds[NUM_LEDS];
void setup() {

  #define LED_PIN     15

  FastLED.addLeds<WS2812, LED_PIN, GRB>(leds, NUM_LEDS);


   for (int i = 0; i <= 9; i++) {
      leds[i] = CRGB ( 0, 0, 0);

    }

    FastLED.show();

    esp_deep_sleep_start();
    
  // initialize serial communication at 9600 bits per second:

}

// the loop routine runs over and over again forever:
void loop() {

}
