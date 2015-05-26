/*

Copyright (c) 2012, 2013 RedBearLab

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/

#include <Servo.h>
#include <SPI.h>
#include <boards.h>
#include <RBL_nRF8001.h>
#include "Boards.h"

#define PROTOCOL_MAJOR_VERSION   0 //
#define PROTOCOL_MINOR_VERSION   0 //
#define PROTOCOL_BUGFIX_VERSION  2 // bugfix

#define PIN_CAPABILITY_NONE      0x00
#define PIN_CAPABILITY_DIGITAL   0x01
#define PIN_CAPABILITY_ANALOG    0x02
#define PIN_CAPABILITY_PWM       0x04
#define PIN_CAPABILITY_SERVO     0x08
#define PIN_CAPABILITY_I2C       0x10

// pin modes
//#define INPUT                 0x00 // defined in wiring.h
//#define OUTPUT                0x01 // defined in wiring.h
#define ANALOG                  0x02 // analog pin in analogInput mode
#define PWM                     0x03 // digital pin in PWM output mode

byte pin_mode[TOTAL_PINS];
byte pin_state[TOTAL_PINS];
byte pin_pwm[TOTAL_PINS];

int ledPin = 13;

Servo servos[MAX_SERVOS];

void setup()
{
    Serial.begin(57600);
    Serial.println("TACTIO: BLE Arduino Slave");

    pinMode(ledPin, OUTPUT);

    // Set your BLE Shield name here, max. length 10
    ble_set_name("tactio");

    // Init. and start BLE library.
    ble_begin();
}

static byte buf_len = 0;

void ble_write_string(byte *bytes, uint8_t len)
{
    if (buf_len + len > 20)
    {
        for (int j = 0; j < 15000; j++)
            ble_do_events();

        buf_len = 0;
    }

    for (int j = 0; j < len; j++)
    {
        ble_write(bytes[j]);
        buf_len++;
    }

    if (buf_len == 20)
    {
        for (int j = 0; j < 15000; j++)
            ble_do_events();

        buf_len = 0;
    }
}

byte reportDigitalInput()
{
  if (!ble_connected())
    return 0;

  static byte pin = 0;
  byte report = 0;

  if (!IS_PIN_DIGITAL(pin))
  {
    pin++;
    if (pin >= TOTAL_PINS)
      pin = 0;
    return 0;
  }

  if (pin_mode[pin] == INPUT)
  {
      byte current_state = digitalRead(pin);

      if (pin_state[pin] != current_state)
      {
        pin_state[pin] = current_state;
        byte buf[] = {'G', pin, INPUT, current_state};
        ble_write_string(buf, 4);

        report = 1;
      }
  }

  pin++;
  if (pin >= TOTAL_PINS)
    pin = 0;

  return report;
}


byte reportPinAnalogData()
{
  if (!ble_connected())
    return 0;

  static byte pin = 0;
  byte report = 0;

  if (!IS_PIN_DIGITAL(pin))
  {
    pin++;
    if (pin >= TOTAL_PINS)
      pin = 0;
    return 0;
  }

  if (pin_mode[pin] == ANALOG)
  {
    uint16_t value = analogRead(pin);
    byte value_lo = value;
    byte value_hi = value>>8;

    byte mode = pin_mode[pin];
    mode = (value_hi << 4) | mode;

    byte buf[] = {'G', pin, mode, value_lo};
    ble_write_string(buf, 4);
  }

  pin++;
  if (pin >= TOTAL_PINS)
    pin = 0;

  return report;
}

byte queryDone = false;

void loop()
{
    while(ble_available())
    {
        byte msg;
        msg = ble_read();
        Serial.write(msg);
        if(msg == '1')
        {
            /*ledStatus = true;*/
            digitalWrite(ledPin, HIGH);
        }
        if(msg == '0')
        {
            /*ledStatus = false;*/
            digitalWrite(ledPin, LOW);
        }

        // send out any outstanding data
        ble_do_events();
        buf_len = 0;

        return; // only do this task in this loop
    }

    // process text data
    while(Serial.available())
    {
        byte d;
        d = Serial.read();
        ble_write(d);

        ble_do_events();
        buf_len = 0;

        return;
    }


    // No input data, no commands, process analog data
    if (!ble_connected())
        queryDone = false; // reset query state

    if (queryDone) // only report data after the query state
    {
        byte input_data_pending = reportDigitalInput();
        if (input_data_pending)
        {
            ble_do_events();
            buf_len = 0;

            return; // only do this task in this loop
        }

        reportPinAnalogData();

        ble_do_events();
        buf_len = 0;

        return;
    }
    ble_do_events();
    buf_len = 0;
}
