/*

Copyright (c) 2012, 2013 RedBearLab

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/

#include <SPI.h>
#include <boards.h>
#include <RBL_nRF8001.h>
#include "Boards.h"

// Morse code includes
#include <avr/pgmspace.h>
#include "MorseEnDecoder.h"

const byte morseOutPin = 13;

// Instantiate Morse object
morseEncoder morseOutput(morseOutPin);


void setup()
{
    Serial.begin(57600);
    Serial.println("TACTIO: BLE Arduino Slave");

    // Set your BLE Shield name here, max. length 10
    ble_set_name("tactio");

    // Setting Morse speed in wpm - words per minute
    morseOutput.setspeed(13);

    // Init. and start BLE library.
    ble_begin();
}

void loop()
{
    morseOutput.encode();

    while(ble_available() && morseOutput.available())
    {
        byte msg = ble_read();
        morseOutput.write(msg);

        // Not strictly needed, but used to get morseSignalString before it is destroyed
        // (E.g. for morse training purposes)
        morseOutput.encode();

        // Also write sent character + Morse code to serial port/monitor
        Serial.write('\n');
        Serial.write(msg);
        // Morse code in morseSignalString is now backwards
        for (int i=morseOutput.morseSignals; i>0; i--)
        {
          Serial.write(morseOutput.morseSignalString[i-1]);
        }

        // send out any outstanding data
        ble_do_events();
        return; // only do this task in this loop
    }

    // process text data
    while(Serial.available())
    {
        byte d = Serial.read();
        ble_write(d);

        ble_do_events();
        return;
    }

    ble_do_events();
}
