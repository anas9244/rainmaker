#include <FastLED.h>
#include <Arduino.h>
#include <analogWrite.h>
#include <Wire.h>

#include "BluetoothSerial.h"


#include "esp_deep_sleep.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

//hi

#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BNO055.h>
#include <utility/imumaths.h>

RTC_DATA_ATTR Adafruit_BNO055 bno = Adafruit_BNO055(55);

//#define LED_PIN     15  // for huzzah
//int voltpin = 35;  // for huzzah
int voltpin = 32;
#define LED_PIN     13
#define NUM_LEDS    10



CRGB leds[NUM_LEDS];

String string;
void init_led(int led_n) {

  for (int i = 0; i < led_n; i++) {
    leds[i] = CRGB ( 0, 0, 5);
  }

  FastLED.show();

}


bool led_mode = 0 ;// 0 for tasks, 1 for breat/time

float  work_leds;
unsigned long break_leds;
unsigned long over_all_time;




float volt = 0;
int last_vlt = 0;
int level = 0;

int last_volt = 0;
unsigned long action_time_bat;

void leds_manage(int i, int start_led, int end_led, bool pos) {


  if (pos == 0) {

    if (end_led != 0) {

      for (int l = start_led; l < end_led; l++) {
        leds[l] = CRGB ( i, i, 0);
      }
    }

    else {
      for (int l = 0; l <= 9; l++) {
        leds[l] = CRGB ( 0, 0, i);
      }

    }
  }
  else if (pos == 1) {
    if (end_led != 9) {
      for (int l = start_led; l > end_led; l--) {
        leds[l] = CRGB ( i, i, 0);
      }
    }
    else {

      for (int l = 0; l <= 9; l++) {
        leds[l] = CRGB ( 0, 0, i);
      }

    }
  }
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
unsigned long interval = 60;

bool state_changed = 0;


void fade_leds(int start_led, int end_led, bool pos)
{

  if (pos == 0) {

    if (end_led == 0) {

    }
    else {
      for (int i = 0; i < start_led; i++) {
        leds[i] = CRGB ( 30, 30, 0);
      }








      //    for (int i = tasks; i < all_tasks; i++) {
      //      leds[i] = CRGB ( 15, 15, 0);
      //    }



      for (int i = end_led; i <= 9; i++) {
        leds[i] = CRGB ( 0, 0, 0);
      }

    }

    //fade_leds(tasks, all_tasks,vertical_orient);




  }
  else if (pos == 1) {


    if (end_led == 9) {

    }
    else {


      for (int i = 9; i > start_led; i--) {
        leds[i] = CRGB ( 30, 30, 0);

      }
      //    for (int i = 9 - tasks; i > (9 - all_tasks); i--) {
      //      leds[i] = CRGB (  15, 15, 0);
      //
      //    }





      for (int i = (end_led); i >= 0; i--) {
        leds[i] = CRGB ( 0, 0, 0);

      }


    }

    //fade_leds(9 - tasks, 9 - all_tasks,vertical_orient);



  }


  unsigned long currentMillis = millis(); // grab current time
  leds_manage(brightness, start_led, end_led, pos);
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
  if (brightness >= 20 )
  { // reverse the direction of the fading at the ends of the fade:
    brightness = 20;
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

    //    for (int i = 0; i < tasks; i++) {
    //      leds[i] = CRGB ( 70, 70, 4);
    //    }
    //
    //
    //    //    for (int i = tasks; i < all_tasks; i++) {
    //    //      leds[i] = CRGB ( 15, 15, 0);
    //    //    }
    //
    //
    //    for (int i = all_tasks; i <= 9; i++) {
    //      leds[i] = CRGB ( 0, 0, 0);
    //    }

    fade_leds(tasks, all_tasks, vertical_orient);

    // FastLED.show();



  }
  else if (vertical_orient == 1) {
    //
    ////    for (int i = 9; i > 9 - tasks; i--) {
    ////      leds[i] = CRGB ( 70, 70, 4);
    ////
    ////    }
    ////    //    for (int i = 9 - tasks; i > (9 - all_tasks); i--) {
    ////    //      leds[i] = CRGB (  15, 15, 0);
    ////    //
    ////    //    }
    ////
    ////
    ////
    ////    for (int i = (9 - all_tasks); i >= 0; i--) {
    ////      leds[i] = CRGB ( 0, 0, 0);
    ////
    ////    }

    //
    fade_leds(9 - tasks, 9 - all_tasks, vertical_orient);
    //
    //
    //    //FastLED.show();
  }

}
void set_led_ratio(unsigned long break_time, unsigned long work_time) {
  if (work_time == 0 and break_time == 0) {


    for (int i = 0; i < 5; i++) {
      leds[i] = CRGB ( 0, 0, 40);
    }

    for (int i = 5; i <= 9; i++) {
      leds[i] = CRGB ( 40, 10, 0);
    }

    FastLED.show();






  } else {


    over_all_time = break_time + work_time;

    Serial.print("overall:");
    Serial.println(over_all_time);

    work_leds = (float(work_time ) / float(over_all_time));

    Serial.print("work leds before norm:");
    Serial.println(work_leds);

    work_leds = work_leds * 10;


    Serial.print("work leds:");
    Serial.println(work_leds);
    //break_leds = (break_time / over_all_time) * 10;


    if (work_leds == 10) {
      work_leds = 9;
    }

    if (work_leds == 0) {
      work_leds = 1;
    }

    for (int i = 0; i < int(work_leds); i++) {
      leds[i] = CRGB ( 0, 0, 40);
    }

    for (int i = int(work_leds); i <= 9; i++) {
      leds[i] = CRGB ( 40, 10, 0);
    }

    FastLED.show();

  }
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


bool turend_on = true;

int all_tasks = 0;

//int all_tasks=0;

int finished_tasks = 0;
//int finished_tasks=0;

unsigned long work_time = 0;
//unsigned long work_time=0;

unsigned long break_time = 0;

unsigned long work_start = 0;

unsigned long break_start = 0;

bool toggle_shake = 0;
//unsigned long break_time=0;


//int all_tasks;

#define uS_TO_S_FACTOR 1000000  /* Conversion factor for micro seconds to seconds */
#define TIME_TO_SLEEP  1

void setup() {
  Serial.begin(115200);



  //pinMode(35, INPUT); // for huzzah !!!!!!
  //pinMode(32, INPUT);



  esp_sleep_enable_timer_wakeup(TIME_TO_SLEEP * uS_TO_S_FACTOR);

  //  if (all_tasks > 10 or all_tasks < 0) {
  //    all_tasks = 0;
  //  }
  //
  //
  //  if (finished_tasks > 10 or finished_tasks < 0  ) {
  //    finished_tasks = 0;
  //  }
  //  if (first_boot) {
  //    first_boot = false;
  //    Serial.println("first Boot!");
  //    finished_tasks = 0;
  //    all_tasks = 0;
  //
  //    //start_timer=millis();
  //
  //    work_time = 0;
  //    break_time = 0;
  //    work_start = 0;
  //    break_start = 0;
  //
  //  }

  //  if (toggle_shake==false){

  SerialBT.begin("The_Rainmaker!");
  Serial.println("bt is ON !!!");

  delay(1000);

  //  }
  //  else{
  //
  //    //turend_on=true;
  //
  //
  //}

  Serial.println("finished" + finished_tasks);





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





bool activated = false;

char command;



bool vertical_orient; // 0 for upright, 1 for upsidedown

bool flipped;




int shakes = 0;
bool shake_direct = 0;

bool shaking = 0;
int last_shake = 0;

int last_val = 0;
unsigned long shake_time;



bool show_bat_lvl = 0;


bool bt_once = false;

bool bt_off_once = true;


bool start_measure = 0;

int deletedTask;


unsigned long start_timer = 0;


unsigned long init_timer = 0;

bool lowBat = false;

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


  if  (millis() - shake_time < 500) {

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


    if (turend_on) {
      turend_on = false;
      leds_off();
      SerialBT.end();
      Serial.println("bt is off!");
      delay(1000);
      //
      //     if (bt_stop()) {
      //        Serial.println("bt is off!");
      //        delay(1000);
      //      }

    }

    //    if (bt_off_once) {
    //      bt_off_once = false;
    //      //SerialBT.flush();
    ////      if (btStop()) {
    ////        Serial.println("bt is off!");
    ////        delay(100);
    ////      }
    //      bt_once = true;


  }
  else {
    if (turend_on == false) {
      turend_on = true;
      //SerialBT.flush();



      if (SerialBT.begin("The_Rainmaker!")) {
        Serial.println("bt is ON !!!");
      }
      delay(1000);






      //leds_off();

      // esp_deep_sleep_start();

    }

    //    if (bt_once) {
    //
    //      bt_once = false;
    //
    //      turend_on=false;
    //      //      if (SerialBT.begin("The_Rainmaker!")) {
    //      //        Serial.println("bt is on!");
    //      //
    //      //      }
    //      //      delay(100);
    //      //
    //      //bt_off_once = true;
    //      //first_boot = false;
    //
    //
    //
    //      //ESP.restart();
    //    }

    if (all_tasks == 0) {
      //fade_leds(0, 9,0);
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
      //
      //      if (string == "B")
      //
      //      {
      //        volt = analogRead(voltpin);
      //        level = map(volt, 1799, 2383, 0, 100);
      //        SerialBT.write(level);
      //      }

      if (string == "R")

      {
        //ESP.restart();

        finished_tasks = 0;



        break_time = 0;
        work_time = 0;
        work_start = 0;
        break_start = 0;


        all_tasks = 0;
        activated = false;

        set_led_tasks(finished_tasks, all_tasks, vertical_orient);


      }



      if (string == "N")
      {
        Serial.println("N");
        //String msg= String ("B"+finished_tasks);

        volt = analogRead(voltpin);
        Serial.println(volt);

        level = map(volt, 1700, 2383, 0, 100);
        SerialBT.write(level);

        //SerialBT.write('T');
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


        if (all_tasks == 0 && finished_tasks == 0 && string.toInt() == 1)  {
          init_timer = millis();
        }

        all_tasks = string.toInt();

        Serial.println("alltasks");
        Serial.println(all_tasks);
        current = map(event.orientation.y, -90, 90, 0, 180);

        Serial.println( current);

        //if (state == 0) {

        if (current > 155 and current < 180) {
          vertical_orient == 0;
          Serial.println("straigght !!");

          set_led_tasks(finished_tasks, all_tasks, vertical_orient);
        }

        if (current > 0 and current < 20) {
          vertical_orient == 1;

          Serial.println("upside down !!");


          set_led_tasks(finished_tasks, all_tasks, vertical_orient);
        }


        if (current > 85 and current < 105) {

          set_led_ratio(break_time, work_time);


        }







        activated = true;


        //}
        delay(10);

      }
      if (string.startsWith("D")) {
        string.remove(0, 1);

        deletedTask = string.toInt() ;

        Serial.println(deletedTask);
        if ((deletedTask + 1) > finished_tasks) {
          //          finished_tasks--;
          //          all_tasks--;
          //        }
          //        else {
          all_tasks--;
        }
        delay(10);

        if (all_tasks == 0) {
          finished_tasks = 0;



          break_time = 0;
          work_time = 0;
          work_start = 0;
          break_start = 0;


          all_tasks = 0;
          activated = false;
        }



        set_led_tasks(finished_tasks, all_tasks, vertical_orient);

        //
      }


    }
    if (activated) {
      current = map(event.orientation.y, -90, 90, 0, 180);

      //Serial.println(all_tasks);



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
          if (state == 1)
          { state_changed = 1;

          }

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

          if (state == 1)
          { state_changed = 1;

          }


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
          start_measure = 1;
        }

        if (current > 85 and current < 105) {
          if (state == 0)
          { state_changed = 1;

          }

          Serial.println("break");
          state = 1;
        }

        if (state == 0) {

          Serial.print("finished tasks");
          Serial.println(finished_tasks);
          Serial.print("all_tasks");
          Serial.println(all_tasks);



          //Serial.println("work");

          if (state_changed) {
            state_changed = 0;
            if (finished_tasks < all_tasks) {


              start_timer = (millis() - init_timer) / 1000;

              work_start = start_timer;

              break_time = break_time + (start_timer - break_start);
            }

          }

          Serial.print("Work_time: ");
          Serial.println(work_time);

          Serial.print("Break_time: ");
          Serial.println(break_time);







          if (flipped) {
            flipped = 0;
            Serial.println("finished_task!!");
            if (finished_tasks < all_tasks) {
              finished_tasks++;
            }

            if (finished_tasks == all_tasks) {
              Serial.println("finished all!");
              //activated=false;
              //state=1;



              //set_led_ratio(break_time, work_time);


            }



            volt = analogRead(voltpin);
            level = map(volt, 1700, 2383, 0, 100);
            //SerialBT.write(level);

            SerialBT.write(level);
            //SerialBT.write('T');
            SerialBT.write(finished_tasks);
            SerialBT.write('\n');

            //set_led_tasks(finished_tasks, all_tasks, vertical_orient);
          }

          //set_led_tasks(finished_tasks, all_tasks, vertical_orient);




        }


        else if (state == 1) {
          //Serial.println("state_change!");
          // Serial.println("break");,


          if (state_changed) {

            if (finished_tasks < all_tasks) {

              state_changed = 0;

              start_timer = (millis() - init_timer) / 1000;

              Serial.print("start_tiomer:");
              Serial.println( start_timer);
              break_start = start_timer;


              work_time = work_time + (start_timer - work_start);
            }
          }

          Serial.print("Work_time: ");
          Serial.println(work_time);

          Serial.print("Break_time: ");
          Serial.println(break_time);




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
  if (turend_on) {

//    if (lowBat == false) {
//
//      if (millis() % 10000 == 0) {
//
//        volt = analogRead(voltpin);
//        level = map(volt, 1700, 2383, 0, 100);
//        //SerialBT.write(level);
//
//        if (level <= 20) {
//
//          lowBat = true;
//
//          SerialBT.write(level);
//          //SerialBT.write('T');
//          SerialBT.write(finished_tasks);
//          SerialBT.write('\n');
//
//        }
//      }
//    }





    if (all_tasks != 0) {

      if (state == 0) {
        set_led_tasks(finished_tasks, all_tasks, vertical_orient);

      }

    }
    else {
      set_led_tasks(finished_tasks, all_tasks, vertical_orient);

    }

  }





}
