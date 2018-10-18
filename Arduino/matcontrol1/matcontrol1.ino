//Libraries to include
//#include <LiquidCrystal.h>
#include <SoftwareSerial.h>


// Define pins

//Demux control pins
const int a0 = 2;     
const int a1 = 3; 
const int a2 = 4;
const int a3 = 5;
//Shift register control pins
const int d = 6;
const int ck = 7;
const int en = 8;
//Initialise LCD display
//LiquidCrystal lcd(14,15,16,17,18,19);

//Initialise SoftwareSerial
SoftwareSerial bluetoothSerial(9,10);
//Define global variables
//4-bit representation of the line selected on the demultimplexer
bool bt[4] = {1,1,1,1}; //Initialise demux on pin 15
//100-bit array corresponding to the array state
int data[100];
char cdata[100];
//Number of the selected row
int rownum = 0;
//Data in selected row
int row[10] = {0,0,0,0,0,0,0,0,0,0};
//Function prototypes
void convertData(char data_in[], int data_out[]);
void num2bit(int num, bool bt[4]);
void muxFlip(int num);
void getRowData(int data[], int num, int row[10]);
int readData(char data[]);
void setData(int data[]);
void shiftTen(int row[]);


//Function definitions

//Converts inbound char array from bluetooth module to array of 0 and 1
void convertData(char data_in[], int data_out[]){
  for(int i = 0; i < 100; i++){
    data_out[i] = data_in[i] - '0';
  }
  
}

//Stores the number in the first argument in the bt[] array
void num2bit(int num, bool bt[4]){
  //Serial.print("Converted: ");
  for (int i = 0; i < 4; i++){
    bt[i] = bitRead(num,i);
    //Serial.print(bt[i]);
  }
  //Serial.print("\n");
}

//Switches the demultiplexer to the row given as the argument
void muxFlip(int num){
  //Serial.print("Called flip to: ");
  //Serial.print(num);
  //Serial.print("\n");
  bool bt[4];
  num2bit(num,bt);
  if (bt[0] == 0){
    digitalWrite(a0, LOW);
    //Serial.print("a0,LOW\n");
  }
  else{
    digitalWrite(a0, HIGH);
    //Serial.print("a0,HIGH\n");
  }
  if (bt[1] == 0){
    digitalWrite(a1, LOW);
    //Serial.print("a1,LOW\n");
  }
  else{
    digitalWrite(a1, HIGH);
    //Serial.print("a1,HIGH\n");
  }
  if (bt[2] == 0){
    digitalWrite(a2, LOW);
    //Serial.print("a2,LOW\n");
  }
  else{
    digitalWrite(a2, HIGH);
    //Serial.print("a2,HIGH\n");
  }
  if (bt[3] == 0){
    digitalWrite(a3, LOW);
    //Serial.print("a3,LOW\n");
  }
  else{
    digitalWrite(a3, HIGH);
    //Serial.print("a3,HIGH\n");
  } 
}

//Fills the 'row' array with the contents of 'data' specified by the 'num' argument
void getRowData(int data[], int num, int row[10]){
  for (int i = 0; i < 10; i++){ 
    row[i] = data[10*num+i];
    //Serial.print(row[i]);
  }
  
  //Serial.print("\n");
}

//Sets the contents of data onto the electrode matrix
void setData(int data[]){
  for (int num = 0; num < 10; num++){
    getRowData(data,num,row);
    digitalWrite(ck,LOW);
    //Serial.print("ck,LOW\n");
    //Shift out low byte
    shiftTen(row);
    digitalWrite(ck,LOW);
    //Serial.print("ck,LOW\n");
    digitalWrite(en, HIGH);
    //Serial.print("en,HIGH\n");
    delay(20);
    muxFlip(num);
    delay(20);
    digitalWrite(en, LOW);
    //Serial.print("EN,LOW\n");
    muxFlip(15);
  }
  //Set mux back to 15 for reliability
  muxFlip(15);
}

//Reads string of characters in from bluetooth, beginning with "<" and ending with ">"
int readData(char data[]){
  int i = 0;
  char d_in;
  if(bluetoothSerial.available()>0){
    while (bluetoothSerial.read() != '<'){}
    d_in = bluetoothSerial.read();
    
    while (d_in != '>'){
    
    data[i] = d_in;
    d_in = bluetoothSerial.read();
    i++;
    }
    return true;
  }
  else{
   return false;
  } 
}

void shiftTen(int row[]){
  for(int i = 0;i<10;i++){
    digitalWrite(ck,LOW);
    //Serial.print("ck, LOW\n");
    digitalWrite(d,row[9-i]);
    //Serial.print("d, ");
    //Serial.print(row[9-i]);
    //Serial.print("\n");
    delay(5);
    digitalWrite(ck,HIGH);
    //Serial.print("ck, HIGH\n");
    delay(5);
  }
  digitalWrite(ck,LOW);
  //Serial.print("ck, LOW\n");
}  

void setup() {
  //Initialise output pins
  pinMode(a0, OUTPUT); 
  pinMode(a1, OUTPUT);
  pinMode(a2, OUTPUT);
  pinMode(a3,OUTPUT);
  pinMode(d, OUTPUT);
  pinMode(ck, OUTPUT);
  pinMode(en, OUTPUT);
  pinMode(13,OUTPUT);
  // initialize the pushbutton pin as an input:
  //pinMode(buttonPin, INPUT);  
  Serial.begin(38400);
  bluetoothSerial.begin(38400);
  //Serial.print("Begin\n");
  

  //Initialise the demux on OUTPUT 15 for reliability
  muxFlip(15);
  //Set Shift register latches to OFF, and output to HIGH IMPEDANCE
  digitalWrite(en, LOW);
  //Serial.print("en,LOW\n");
  
  //TEST CODE
//  for (int i = 0; i < 100; i++){
//    if (i%2 == j){
//      data[i] = 1;
//    }
//    else{
//      data[i] = 0;
//    }
//  }
//  setData(data);
}
  
void loop(){

if(readData(cdata)){
  convertData(cdata, data);
  setData(data);
}



}


