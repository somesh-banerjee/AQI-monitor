#include <TinyGPS.h>
#include <ESP8266WiFi.h>

#define WIFI_SSID "" // input your home or public wifi SSID
#define WIFI_PASSWORD "" //wifi password

String apiKey = ""; //key to write to the server
const char* server = "api.thingspeak.com";

WiFiClient client;

int mq135 = A0; // smoke sensor is connected with the analog pin A0 
int data = 0;
 
void setup() 
{
  Serial.begin(115200);
  delay(1000);                
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);                                     //try to connect with wifi
  //try to connect with wifi
  Serial.print("Connecting to ");
  Serial.print(WIFI_SSID);
  while (WiFi.status() != WL_CONNECTED) {
  Serial.print(".");
  delay(500);
  }
  Serial.println();
  Serial.print("Connected to ");
  Serial.println(WIFI_SSID);
  Serial.print("IP Address is : ");
  Serial.println(WiFi.localIP());
  //Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);                              // connect to firebase
}
 
void loop() 
{
  data = analogRead(mq135);
  Serial.println(data);
  delay(2000);
  //Firebase.pushString("/IOT/AQI", data);                                  //setup path and send readings
  if (client.connect(server,80)) // "184.106.153.149" or api.thingspeak.com
  {
	String postStr = apiKey;
	postStr +="&field1=";
	postStr += String(data);
	postStr += "\r\n\r\n";
	
	client.print("POST /update HTTP/1.1\n");
	client.print("Host: api.thingspeak.com\n");
	client.print("Connection: close\n");
	client.print("X-THINGSPEAKAPIKEY: "+apiKey+"\n");
	client.print("Content-Type: application/x-www-form-urlencoded\n");
	client.print("Content-Length: ");
	client.print(postStr.length());
	client.print("\n\n");
	client.print(postStr);
	
	Serial.print("AQI: ");
	Serial.print(data);
	Serial.println("%. Send to Thingspeak.");
  }
  client.stop();
  
  Serial.println("Waiting...");
  
  // thingspeak needs minimum 15 sec delay between updates
  delay(3600000);
 }