#include <FastLED.h>
#include <Arduino.h>
#include <analogWrite.h>
#include <Wire.h>

#include "BluetoothSerial.h"


#include "esp_deep_sleep.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BNO055.h>
#include <utility/imumaths.h>

RTC_DATA_ATTR Adafruit_BNO055 bno = Adafruit_BNO055(55);

#define LED_PIN     15
#define NUM_LEDS    10



CRGB leds[NUM_LEDS];

String string;
void init_led(int led_n) {

  for (int i = 0; i < led_n; i++) {
    leds[i] = CRGB ( 0, 0, 5);
  }

  FastLED.show();

}

int finished_tasks = 0;
bool led_mode = 0 ;// 0 for tasks, 1 for breat/time

long work_leds;
int break_leds = 5;
float over_all_time;


int voltpin = 35;

float volt = 0;
int last_vlt = 0;
int level = 0;

int last_volt = 0;
unsigned long action_time_bat;

void leds_manage(int i) {
  leds[0] = CRGB ( 0, 0, i);
  leds[1] = CRGB ( 0, 0, i);
  leds[2] = CRGB ( 0, 0, i);
  leds[3] = CRGB ( 0, 0, i);
  leds[4] = CRGB ( 0, 0, i);
  leds[5] = CRGB ( 0, 0, i);
  leds[6] = CRGB ( 0, 0, i);
  leds[7] = CRGB ( 0, 0, i);
  leds[8] = CRGB ( 0, 0, i);
  leds[9] = CRGB ( 0, 0, i);
}

void leds_off() {
  for (int i = 0; i <= 9; i++) {
    leds[i] = CRGB ( 0, 0, 0);
  }
  FastLED.show();
}


void leds_on() {
  for (int i = 0; i <= 9; i++) {
    leds[i] = CRGB ( 0, 0, 50);
  }
  FastLED.show();
}

unsigned long last_fade_on = 0;
unsigned long last_fade_off = 0;

int brightness = 0;    // how bright the LED is
int fadeAmount = 1;    // how many points to fade the LED by
unsigned long previousMillis = 0;
unsigned long interval = 100;


void fade_leds()
{


  unsigned long currentMillis = millis(); // grab current time
  leds_manage(brightness);
  FastLED.show();   // set the brightness of ledPin:

  if (currentMillis - previousMillis >= interval) {
    brightness = brightness + fadeAmount;     // change the brightness for next time through the loop:
    previousMillis = millis();
  }


  if (brightness <= 0 )
  { // reverse the direction of the fading at the ends of the fade:
    brightness = 0;
    fadeAmount = -fadeAmount;
  }
  if (brightness >= 15 )
  { // reverse the direction of the fading at the ends of the fade:
    brightness = 15;
    fadeAmount = -fadeAmount;
  }




  //unsigned long start_fade = millis();




  //  for (int i = 0; i <= 25; i++) {
  //    if (millis() - last_fade_on > 1000)
  //    {
  //      last_fade_on = millis();
  //      leds_manage(i);
  //      FastLED.show();
  //      //delay(100);
  //    }
  //
  //  }
  //
  //  for (int i = 25; i >= 0; i--) {
  //
  //    if (millis() - last_fade_off > 2000)
  //    {
  //
  //      last_fade_off = millis();
  //      leds_manage(i);
  //      FastLED.show();
  //
  //      //delay(100);
  //
  //    }
  //
  //  }

  //delay(200);
}



void set_led_tasks(int  tasks, int all_tasks, bool vertical_orient) {


  if (vertical_orient == 0) {

    for (int i = 0; i <= tasks; i++) {
      leds[i] = CRGB ( 0, 0, 255);
    }

    for (int i = tasks; i <= all_tasks; i++) {
      leds[i] = CRGB ( 0, 0, 5);
    }

    for (int i = all_tasks; i <= 9; i++) {
      leds[i] = CRGB ( 0, 0, 0);
    }

    FastLED.show();
  }
  else {

    for (int i = 9; i >= 9 - tasks; i--) {
      leds[i] = CRGB ( 0, 0, 255);

    }

    for (int i = 9 - tasks; i >= (9 - all_tasks); i--) {
      leds[i] = CRGB ( 0, 0, 5);

    }

    for (int i = (9 - all_tasks); i >= 0; i--) {
      leds[i] = CRGB ( 0, 0, 0);

    }
    FastLED.show();
  }

}
void set_led_ratio(unsigned long break_time, unsigned long work_time) {

  over_all_time = break_time + work_time;

  work_leds = (work_time / over_all_time) * 10;
  //break_leds = (break_time / over_all_time) * 10;


  if (work_leds == 10) {
    work_leds = 9;
  }
  for (int i = 0; i <= int(work_leds); i++) {
    leds[i] = CRGB ( 80, 0, 0);
  }

  for (int i = int(work_leds); i <= 9; i++) {
    leds[i] = CRGB ( 0, 80, 0);
  }

  FastLED.show();
}




void led_bat_lvl(int bat_lvl) {


  for (int i = 0; i < bat_lvl; i++) {
    leds[i] = CRGB ( 255, 255, 0);
  }
  for (int i = bat_lvl; i <= 9; i++) {
    leds[i] = CRGB ( 0, 0, 0);
  }

  FastLED.show();

}





BluetoothSerial SerialBT;
//RTC_NOINIT_ATTR int all_tasks;
int all_tasks;

void setup() {

  pinMode(35, INPUT);

  if (all_tasks > 10) {
    all_tasks = 0;
  }



  Serial.begin(115200);
  SerialBT.begin("The_Rainmaker!");



  //pinMode(LED_PIN, OUTPUT);
  FastLED.addLeds<WS2812, LED_PIN, GRB>(leds, NUM_LEDS);

  //  for (int i = 0; i <= 9 ; i++) {
  //    leds[i] = CRGB ( 0, 0, 0);
  //
  //  }



  //init_led(6);
  /* Initialise the sensor */
  if (!bno.begin())
  {
    /* There was a problem detecting the BNO055 ... check your connections */
    Serial.print("Ooops, no BNO055 detected ... Check your wiring or I2C ADDR!");
    while (1);

  }

  delay(10);

  bno.setExtCrystalUse(true);
}


int last = 0;
int current = 0;

int current_shake = 0;


unsigned long action_time;

bool start_action = 1;

int val_start;

bool state = 0; // 0 for work, 1 for break


unsigned long break_time = millis();
unsigned long work_time = millis();

unsigned long work_start;
unsigned long break_start;

bool activated = false;

char command;



bool vertical_orient = 0; // 0 for upright, 1 for upsidedown

bool flipped = 0;




int shakes = 0;
bool shake_direct = 0;

bool shaking = 0;
int last_shake = 0;

int last_val = 0;
unsigned long shake_time;

bool toggle_shake = 0;

bool show_bat_lvl = 0;


bool bt_once = false;

bool bt_off_once = true;




int deletedTask;

void loop() {



  sensors_event_t event;
  bno.getEvent(&event);

  current_shake = map(event.orientation.y, -90, 90, 0, 180);


  if (current_shake > last_val) {
    if (shake_direct == 0 and abs(current_shake - last_shake) > 8 ) {
      Serial.println (shakes);
      shakes++;
      shake_direct = 1;
      last_shake = current_shake;
    }
  }

  if (current_shake < last_val) {
    if (shake_direct == 1 and abs(current_shake - last_shake ) > 8 ) {
      Serial.println (shakes);
      shakes++;
      shake_direct = 0;
      last_shake = current_shake;
    }
  }

  last_val = current_shake;

  if (shakes == 1) {
    shake_time = millis();
  }


  if  (millis() - shake_time < 400) {

    if (shakes == 4)  {
      delay(500); // very important !!!
      shakes = 0;
      if (toggle_shake == 0 ) {
        toggle_shake = 1;
      }
      else if (toggle_shake == 1 ) {
        toggle_shake = 0;
      }


      Serial.println ("shaking");
    }
  }
  else {
    shakes = 0;
  }




  if (toggle_shake) {

    leds_off();

    if (bt_off_once) {
      bt_off_once = false;
      SerialBT.flush();
      if (btStop()) {
        Serial.println("bt is off!");
        delay(100);
      }
      bt_once = true;
    }


    //Serialbt.flush();



    //    esp_bluedroid_disable();
    //    esp_bluedroid_deinit();
    //    esp_bt_controller_disable();
    //    esp_bt_controller_deinit();
  }
  else {

    if (bt_once) {
      //      bt_once = false;
      //      if (SerialBT.begin("The_Rainmaker!")) {
      //        Serial.println("bt is on!");
      //
      //      }
      //      delay(100);
      //
      bt_off_once = true;

      ESP.restart();
    }

    if (all_tasks == 0) {
      fade_leds();
    }
    else {
      activated = true;
    }





    if (SerialBT.available() > 0) {

      {
        string = "";
      }


      while (SerialBT.available() > 0)
      {
        command = ((byte)SerialBT.read());

        if (command == ':')
        {
          break;
        }

        else
        {
          string += command;
        }

        delay(1);
      }


      if (string == "TO")

      {
        Serial.println("ON is sent!!");
        activated = true;
      }

      if (string == "B")

      {
        volt = analogRead(voltpin);
        level = map(volt, 1799, 2383, 0, 100);
        SerialBT.write(level);
      }

      if (string == "N")
      {
        Serial.println("N");
        //String msg= String ("B"+finished_tasks);

        volt = analogRead(voltpin);
        level = map(volt, 1799, 2383, 0, 100);
        //SerialBT.write(level);

        SerialBT.write(level);
        SerialBT.write('T');
        SerialBT.write(finished_tasks);
        SerialBT.write('\n');
        
      }


      if (string == "TF")
      {
        Serial.println("off is sent!");

        for (int i = 0; i <= 9; i++) {
          leds[i] = CRGB ( 0, 0, 0);
        }
        FastLED.show();

        activated = false;
      }

      if ((string.toInt() > 0) && (string.toInt() <= 11))
      {
        if (string.toInt() == 11) {
          all_tasks = 0;
          activated = false;
          break_time = 0;
          work_time = 0;





        }
        else {
          all_tasks = string.toInt();

          Serial.println("alltasks");
          Serial.println(all_tasks);

          if (state == 0) {

            set_led_tasks(finished_tasks, all_tasks, vertical_orient);
          }

          activated = true;


        }
        delay(10);

      }
      if (string.startsWith("D")) {
        string.remove(0, 1);

        deletedTask = string.toInt() ;

        Serial.println(deletedTask);
        if ((deletedTask + 1) <= finished_tasks) {
          finished_tasks--;
          all_tasks--;
        }
        else {
          all_tasks--;
        }
        delay(10);

        if (state == 0) {

          set_led_tasks(finished_tasks, all_tasks, vertical_orient);
        }
        //
      }


    }
    if (activated) {

      //Serial.println(all_tasks);

      current = map(event.orientation.y, -90, 90, 0, 180);

      if (abs(current - last) > 5) {
        if (start_action == 1 ) {
          Serial.println("action start!");

          val_start = last;
          start_action = 0;

        }

        last = current;
        action_time = millis();
        Serial.println(current);

      }



      if (millis() - action_time > 700 and start_action == 0) {
        Serial.println("action finish!");
        start_action = 1;
        //Serial.println(abs(current - val_start));

        // work_normal= 160-->175
        //break= 90-->105
        //work_upside down=  0-15


        //if (abs(current - val_start) > 130 ) {
        if (current > 155 and current < 180) {
          Serial.println("work");
          if (state == 0) {
            if (vertical_orient == 1) {
              flipped = 1;
            } else {
              flipped = 0;
            }
          }
          state = 0;
          vertical_orient = 0;
        }

        if (current > 0 and current < 20) {

          Serial.println("work");

          if (state == 0) {
            if (vertical_orient == 0) {
              flipped = 1;
            } else {
              flipped = 0;
            }
          }
          state = 0;
          vertical_orient = 1;
        }

        if (current > 85 and current < 105) {

          Serial.println("break");
          state = 1;
        }

        if (state == 0) {
          //Serial.println("work");
          work_start = millis();
          break_time += millis() - break_start;

          if (flipped) {
            flipped = 0;
            Serial.println("finished_task!!");
            finished_tasks++;
          }

          set_led_tasks(finished_tasks, all_tasks, vertical_orient);
        }


        else if (state == 1) {
          //Serial.println("state_change!");
          // Serial.println("break");

          break_start = millis();


          work_time += millis() - work_start;

          //Serial.println(work_time / 1000);

          set_led_ratio(break_time, work_time);


          // }

          // else if (state == 1) {
          //state = 0;


          //Serial.println(break_time / 1000);


          //}
        }

      }

      //}

      //    if (state == 1) {
      //      break_time += millis() - break_time;
      //      //if ((millis() - break_time) % 1000 == 0) {
      //
      //
      //      set_led_ratio(break_time, work_time);
      //      //}
      //
      //    }
      //
      //    if (state == 0) {
      //      work_time += millis() - work_start;
      //      //if ((millis() - break_time) % 1000 == 0) {
      //
      //
      //
      //      set_led_tasks(finished_tasks);
      //      //}
      //
      //    }

      //delay(100);

    }

    /* New line for the next sample */


    /* Wait the specified delay before requesting nex data */




    //  for (int i = 0; i <= 9; i++) {
    //    leds[i] = CRGB ( 0, 0, 255);
    //    FastLED.show();
    //    delay(40);
    //  }
    //  for (int i = 9; i >= 0; i--) {
    //    leds[i] = CRGB ( 0, 255, 0);
    //    FastLED.show();
    //    delay(40);
    //  }
    //
    //  for (int i = 0; i <= 9; i++) {
    //    leds[i] = CRGB ( 255, 0, 0);
    //    FastLED.show();
    //    delay(40);
    //  }




    /* Display the floating point data */




  }


}
