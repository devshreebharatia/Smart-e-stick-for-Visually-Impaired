
#include <SoftwareSerial.h>

SoftwareSerial mySerial(10, 11); // RX, TX

const int trigPin1 = 13;
const int echoPin1 = 12;

const int trigPin2 = 9;
const int echoPin2 = 8;

const int buzzPin = 7;

const int buttonPin = 2;
int value;

char data;
String string = "";
boolean ledon = false;




long duration1;
int distance1;

long duration2;
int distance2;

int d1 = 100;
int d2 = 100;
int ds1 = d1-50;

void setup() {

  Serial.begin(9600);
  
  mySerial.begin(9600);

  pinMode(7,OUTPUT);

  pinMode(buttonPin,INPUT);


  pinMode(trigPin1, OUTPUT); // Sets the trigPin as an Output
  pinMode(echoPin1, INPUT); // Sets the echoPin as an Input

  pinMode(trigPin2, OUTPUT); // Sets the trigPin as an Output
  pinMode(echoPin2, INPUT); // Sets the echoPin as an Input

  pinMode(buzzPin,OUTPUT);

  pinMode(LED_BUILTIN,OUTPUT);
}


void ledOn()
   {
        digitalWrite(LED_BUILTIN,HIGH);
        delay(10);
    }
 
 void ledOff()
 {
        digitalWrite(LED_BUILTIN,LOW);
        delay(10);
      
      
 }


void loop() { 

    string = "";
    value = 0;


    value = digitalRead(buttonPin);
    if(value == 1)
    {
      mySerial.write("speak\n");
      delay(300);
    }
    
    while(mySerial.available() > 0)
    {
      //Serial.println("IN");
      data = mySerial.read();
      string += data;
      
      delay(1);
    }

    if(string.length() != 0)
    {
      Serial.println("Data : "+string);      
    }
    
    if(string.equals("turn on"))
    {
        Serial.println("ON");
        ledOn();
        string = "";
    }
    
    if(string.equals("turn off"))
    {
        ledOff();
        string = "";
    }



  


  distance1 = 200;
  
digitalWrite(trigPin1, LOW);
delayMicroseconds(2);
digitalWrite(trigPin1, HIGH);
delayMicroseconds(10);
digitalWrite(trigPin1, LOW);
duration1 = pulseIn(echoPin1, HIGH);
distance1= duration1*0.034/2;


if(distance1 <= d1)
{
  if(distance1 < ds1)
  {
    tone(buzzPin,1000,50);
    //delay(50);
    //digitalWrite(buzzPin,LOW);
    Serial.println(distance1);
    delay(50);
  }else
  {
    tone(buzzPin,1000,150);
    //delay(150);
    //digitalWrite(buzzPin,LOW);
    Serial.println(distance1);
    delay(150);
  }
}
else
{
  distance2 = 200;

  digitalWrite(trigPin2, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin2, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin2, LOW);
  duration2 = pulseIn(echoPin2, HIGH);
  distance2= duration2*0.034/2;

  if(distance2 <=d2)
  {
      Serial.println(distance2);
      tone(buzzPin,500,400);
      //delay(400);
      //digitalWrite(buzzPin,LOW);
      Serial.println(distance2);
      delay(20);
    
  
  } 
}










    
}
