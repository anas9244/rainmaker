/*
  Analog Input

  Demonstrates analog input by reading an analog sensor on analog pin 0 and
  turning on and off a light emitting diode(LED) connected to digital pin 13.
  The amount of time the LED will be on and off depends on the value obtained
  by analogRead().

  The circuit:
  - potentiometer
    center pin of the potentiometer to the analog input 0
    one side pin (either one) to ground
    the other side pin to +5V
  - LED
    anode (long leg) attached to digital output 13
    cathode (short leg) attached to ground

  - Note: because most Arduinos have a built-in LED attached to pin 13 on the
    board, the LED is optional.

  created by David Cuartielles
  modified 30 Aug 2011
  By Tom Igoe

  This example code is in the public domain.

  http://www.arduino.cc/en/Tutorial/AnalogInput
*/

#include <FastLED.h>
#include <Arduino.h>
//#include <analogWrite.h>
#include <Wire.h>


// select the input pin for the potentiometer
int voltpin = 35;      // select the pin for the LED

#define LED_PIN     15
#define NUM_LEDS    10

CRGB leds[NUM_LEDS];

void set_led(int bat_lvl) {


  for (int i = 0; i <= bat_lvl; i++) {
    leds[i] = CRGB ( 255, 0, 0);
  }
  for (int i = bat_lvl; i <= 9; i++) {
    leds[i] = CRGB ( 0, 0, 0);
  }

  FastLED.show();

}
// variable to store the value coming from the sensor
float volt = 0;
int last = 0;
int level = 0;


void setup() {
  Serial.begin(115200);
  // declare the ledPin as an OUTPUT:
  pinMode(voltpin, INPUT);
  FastLED.addLeds<WS2812, LED_PIN, GRB>(leds, NUM_LEDS);
}

int last_volt = 0;
unsigned long action_time;

void loop() {

  // read the value from the sensor:
  volt = analogRead(voltpin);


  level = map(volt, 1799, 2383, 0, 10);
  //level = map(level, 580, 774, 0, 100);

  if (volt > last_volt)
  {
    action_time = millis();

    if (last != level) {
      set_led(level);
    }
  }




  last = level;

  Serial.println(volt);
  Serial.println(level);




  if (millis() - action_time > 3000 and volt < last_volt) {
    for (int i = 0; i <= 9; i++) {
      leds[i] = CRGB ( 0, 0, 0);
    }

    FastLED.show();
  }


  last_volt = volt;

  delay(1000);



}
