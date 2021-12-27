package com.devansh.smartstick;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //widgets
    Button btnCon;
    TextView txtView,navText;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    Context context;
    TextToSpeech speech;

    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";



    Thread workerThread,connectThread,patternThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    volatile boolean stop;

    boolean msg = false;
    boolean tellMyLoc=false;
    String spkName,spkNo=null;




    BluetoothSocket btSocket = null;
    InputStream inputStream = null;
    OutputStream outputStream = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private SpeechRecognizer sr;

    String[] contactName;
    String[] contactNumber;
    int limit=400;
    Cursor cursor;

    TextToSpeech t1;

    //public static String myLocation;
    Location location = new Location();
    public String loc;





    public static String pattern="";
    Timer timer;
    boolean running=false;
    boolean Match=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.navText = (TextView) findViewById(R.id.nav_text);

        final GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                promptSpeechInput1();
                return true;
            }
        };

        final GestureDetector detector = new GestureDetector(context, listener);

        detector.setOnDoubleTapListener(listener);
        detector.setIsLongpressEnabled(true);

        getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });

        //Calling widgets
        //btnPaired = (Button)findViewById(R.id.button);
        this.btnCon = (Button)findViewById(R.id.btnCon);

        //Send = (Button)findViewById(R.id.Send);
        //devicelist = (ListView)findViewById(R.id.listView);
        this.txtView = (TextView) findViewById(R.id.txtView);
        //editText = (EditText) findViewById(R.id.editText);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            String requiredPermission = Manifest.permission.CALL_PHONE;

            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 105);
            }

        }


        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }


        new Thread(new Runnable() {

            @Override
            public void run() {
                getContacts();
            }
        }).start();



        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        }
        else if(!myBluetooth.isEnabled())
        {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }


        //beginListenForPattern();
        beginlistenforConnection();

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

/*
                try
                {
                    btSocket.getOutputStream().write(editText.getText().toString().getBytes());
                }
                catch (IOException e)
                {
                    Toast.makeText(MainActivity.this,""+e,Toast.LENGTH_SHORT).show();
                    txtView.setText("Error");
                }
 */


        btnCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                try
                {
                    if (btSocket == null || !isBtConnected)
                    {
                        myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                        BluetoothDevice dispositivo = myBluetooth.getRemoteDevice("00:18:E4:34:D2:11");//connects to the device's address and checks if it's available
                        btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        btSocket.connect();
                        inputStream = btSocket.getInputStream();//start connection
                        outputStream = btSocket.getOutputStream();
                        txtView.setText("Connect : ");
                        beginListenForData();
                    }
                }
                catch (IOException e)
                {
                    Toast.makeText(MainActivity.this,""+e,Toast.LENGTH_SHORT).show();//if the try failed, you can check the exception here
                    txtView.setText("Not Connected");
                }


            }
        });

    }

    /*
    void beginListenForPattern()
    {

        final Handler handler = new Handler();
        stop=false;

        patternThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while(!stop)
                {

                    if(Thread.currentThread().isInterrupted())
                    {
                        Thread.currentThread().stop();
                        break;
                    }
                    System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOo"+Match);
                    //System.out.println(Match);

                    try {

                        if (Match) {
                            Match = false;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    promptSpeechInput();
                                }
                            });
                        }
                    }
                    catch (Exception e)
                    {
                        System.out.println("##########################################################################");
                    }
                }

            }
        });

        patternThread.start();

    }
*/

    void beginlistenforConnection()
    {
        final Handler handler = new Handler();
        stop = false;

        connectThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stop)
                {

                    try
                    {

                        //System.out.println("00000000000000000");
                        System.out.println("00000000000000000");
                        myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                        BluetoothDevice dispositivo = myBluetooth.getRemoteDevice("00:18:E4:34:D2:11");//connects to the device's address and checks if it's available
                        btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        btSocket.connect();
                        inputStream = btSocket.getInputStream();//start connection
                        outputStream = btSocket.getOutputStream();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                txtView.setText("Connected ");
                                beginListenForData();
                            }
                        });


                        stop = true;
                    }
                    catch (IOException e)
                    {
                        //Toast.makeText(MainActivity.this,""+e,Toast.LENGTH_SHORT).show();//if the try failed, you can check the exception here
                        //txtView.setText("Not Connected");
                        //myBluetooth.cancelDiscovery();
                    }




                }
            }
        });

        connectThread.start();
    }


    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                    try {
                        inputStream = btSocket.getInputStream();
                    } catch (IOException e) {
                        //Toast.makeText(MainActivity.this, "begin btSocket " + e, Toast.LENGTH_SHORT).show();
                    }

                    try {
                        int bytesAvailable = inputStream.available();

                        if (bytesAvailable != 0)
                            //System.out.println("!!!!!!!!!!!!!!!!!!!!        "+bytesAvailable+"          !!!!!!!!!!!!!!!!!");

                            if (bytesAvailable > 0) {
                                //System.out.println("                          INSIDE1                  ");
                                byte[] packetBytes = new byte[bytesAvailable];
                                //System.out.println("                          INSIDE2                  ");
                                inputStream.read(packetBytes);
                                //System.out.println("                          INSIDE3                  ");
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    //System.out.println("                          FOR                    ");
                                    if (b == delimiter) {
                                        //System.out.println("                          IF                  ");
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        handler.post(new Runnable() {
                                            public void run() {
                                                txtView.setText(data);
                                                inputCheck(data);
                                            }
                                        });
                                    } else {
                                        //System.out.println("                          ELSE                  ");
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();

    }


    void inputCheck(String data)
    {

        if(data.equals("speak"))
        {
            promptSpeechInput();
        }

    }

    private void promptSpeechInput() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            String requiredPermission = Manifest.permission.RECORD_AUDIO;

            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 101);
            }
        }


        sr = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
        sr.setRecognitionListener(new listener());


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        sr.startListening(intent);




    }



    class listener implements RecognitionListener
    {
        public void onReadyForSpeech(Bundle params)
        {
        }
        public void onBeginningOfSpeech()
        {
        }
        public void onRmsChanged(float rmsdB)
        {
        }
        public void onBufferReceived(byte[] buffer)
        {
        }
        public void onEndOfSpeech()
        {
        }
        public void onError(int error)
        {
            txtView.setText("error " + error);
            sendData("No data");
        }
        public void onResults(Bundle results) {
            String str = new String();

            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            /*
            for (int i = 0; i < data.size(); i++)
            {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i);
            }
            */
            String text = data.get(0).toString().toLowerCase();

            if (msg) {
                msg = false;
                txtView.setText("Message : " + text + ". Send to - " + spkName + " , " + spkNo);
                sendSMS(spkNo,text);

            }
            else
            {

                boolean msgCheck = text.contains("send text to");
                boolean callCheck = text.contains("call");
                boolean msgLoc = text.contains("send my location to");
                boolean myLoc = text.contains("tell me my location");

                if (msgCheck) {
                    msgCheck=false;
                    int index = text.indexOf("send text to");
                    String name = text.substring(index + 13);
                    int i;
                    for (i = 0; i < limit; i++) {
                        String cName = contactName[i];
                        //System.out.println("("+cName+"),("+name+")");
                        boolean check = cName.equals(name);
                        if (check) {
                            String cNum = contactNumber[i];
                            txtView.setText(cName + "," + cNum);
                            spkName = cName;
                            spkNo = cNum;
                            msg = true;

                            try
                            {
                                t1.speak("Message will be recorded after the beep", TextToSpeech.QUEUE_FLUSH, null);
                                Thread.sleep(4000);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }


                            promptSpeechInput();


                            break;
                        }
                    }
                    if (i == limit) {
                        String s = "No contacts with name " + name;
                        txtView.setText(s);
                        t1.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                else if (callCheck) {
                    callCheck=false;
                    int index = text.indexOf("call");
                    String name = text.substring(index + 5);
                    int i;
                    for (i = 0; i < limit; i++) {
                        String cName = contactName[i];
                        //System.out.println("("+cName+"),("+name+")");
                        boolean check = cName.equals(name);
                        if (check) {
                            String cNum = contactNumber[i];
                            txtView.setText(cName + "," + cNum);
                            spkName = cName;
                            spkNo = cNum;
                            try
                            {
                                t1.speak("Calling "+cName, TextToSpeech.QUEUE_FLUSH, null);
                                Thread.sleep(2000);
                            }
                            catch (Exception e)
                            {
                            }

                            call();


                            break;
                        }
                    }
                    if (i == limit) {
                        String s = "No contacts with name " + name;
                        txtView.setText(s);
                        try
                        {
                            Thread.sleep(2000);
                            t1.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        catch (Exception e)
                        {
                        }

                    }
                }
                else if (msgLoc) {
                    msgLoc=false;
                    int index = text.indexOf("send my location to");
                    String name = text.substring(index + 20);
                    int i;
                    for (i = 0; i < limit; i++) {
                        String cName = contactName[i];
                        //System.out.println("("+cName+"),("+name+")");
                        boolean check = cName.equals(name);
                        if (check) {
                            String cNum = contactNumber[i];
                            txtView.setText(cName + "," + cNum);
                            spkName = cName;
                            spkNo = cNum;
                            t1.speak("Sending location to "+cName, TextToSpeech.QUEUE_FLUSH, null);



                            location.getLocation();


                            break;
                        }
                    }
                    if (i == limit) {
                        String s = "No contacts with name " + name;
                        txtView.setText(s);
                        try
                        {
                            Thread.sleep(2000);
                            t1.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        catch (Exception e)
                        {
                        }

                    }
                }
                else if(myLoc)
                {
                    myLoc=false;
                    tellMyLoc = true;
                    location.getLocation();
                }
                else {
                    txtView.setText(text);
                    sendData(text);
                }

            }


        }
        public void onPartialResults(Bundle partialResults)
        {
        }
        public void onEvent(int eventType, Bundle params)
        {
        }
    }



    void sendData(String data)
    {
        try
        {
            btSocket.getOutputStream().write(data.toString().getBytes());
        }
        catch (IOException e)
        {
            Toast.makeText(MainActivity.this,""+e,Toast.LENGTH_SHORT).show();
            txtView.setText("Error");
        }
    }


    public void getContacts() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            String requiredPermission = Manifest.permission.READ_CONTACTS;

            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 102);
            }

        }

        contactName = new String[400];
        contactNumber = new String[400];

        //init();

        String phoneNumber = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        String output="";

        ContentResolver contentResolver = getContentResolver();

        cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {

            int i=0;
            while (cursor.moveToNext()) {

                // Update the progress message

                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {

                    contactName[i] = name.toLowerCase();

                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);

                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        int len=phoneNumber.length();
                        if(len>10)
                        {
                            contactNumber[i] = phoneNumber.substring(len-10);
                        }
                        else {
                            contactNumber[i] = phoneNumber;
                        }

                    }

                    phoneCursor.close();
                }

                // Add the contact to the ArrayList
                i++;
            }
            limit = i;

        }

        //txtView.setText("Contacts Done");
    }


    public void sendSMS(String phoneNumber, String message)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            // If the user previously denied this permission then show a message explaining why
            // this permission is needed

            String requiredPermission = Manifest.permission.READ_PHONE_STATE;

            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 10);
            }


            requiredPermission = Manifest.permission.SEND_SMS;

            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 0);
            }

        }

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);

        try
        {
            t1.speak("Message send", TextToSpeech.QUEUE_FLUSH, null);
            Thread.sleep(1000);
        }
        catch (Exception e)
        {

        }
    }







    public class Location implements LocationListener {

        LocationManager locationManager;


        void getLocation() {

            try {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
            }
            catch(SecurityException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onLocationChanged(android.location.Location location) {

            loc = "Latitude: " + location.getLatitude() + " ,Longitude: " + location.getLongitude()+".";

            try {
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                loc += "Address : ";
                String l1,l2,l3,l="";
                l1 = addresses.get(0).getAddressLine(0);
                l2 = addresses.get(0).getAddressLine(1);
                l3 = addresses.get(0).getAddressLine(2);

                if(l1!=null)
                {
                    l+=l1;
                }
                if(l2!=null)
                {
                    l+=","+l2;
                }
                if(l3!=null)
                {
                    l+=","+l3;
                }

                loc+=l;

                if(tellMyLoc)
                {
                    tellMyLoc = false;
                    t1.speak("Your Location is : "+l, TextToSpeech.QUEUE_FLUSH, null);
                }
                else
                {
                    txtView.setText(loc);
                    sendSMS(spkNo, loc);
                }

            }catch(Exception e)
            {

                if(tellMyLoc)
                {
                    tellMyLoc = false;
                    t1.speak("Your Location is : "+loc, TextToSpeech.QUEUE_FLUSH, null);
                }
                else {
                    txtView.setText(loc);
                    sendSMS(spkNo, loc);
                }

            }

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

            loc = "Please Enable GPS and Internet";

            if(tellMyLoc)
            {
                tellMyLoc = false;
                t1.speak(loc, TextToSpeech.QUEUE_FLUSH, null);
            }
            else {
                txtView.setText(loc);
                t1.speak(loc, TextToSpeech.QUEUE_FLUSH, null);
            }

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }



    public void call()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            String requiredPermission = Manifest.permission.CALL_PHONE;

            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 105);
            }

        }



        if (!TextUtils.isEmpty(spkNo)) {
            String dial = "tel:" + spkNo;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }else {

            t1.speak("Invalid Mobile Number", TextToSpeech.QUEUE_FLUSH, null);

        }


    }





/*
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                    //txt.setText("Up");

                    if(running)
                    {
                        pattern+="1";
                        //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+pattern);
                    }
                    else
                    {

                        timer = new Timer();
                        timer.schedule(new patternCheck(),5000);
                        running=true;
                        pattern+="1";
                        //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+pattern);

                    }
                }
                return super.dispatchKeyEvent(event);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                    //txt.setText("Down");
                    if(running)
                    {
                        pattern+="0";
                        //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+pattern);
                    }
                }
                return super.dispatchKeyEvent(event);
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    class patternCheck extends TimerTask
    {

        @Override
        public void run() {
            timer.cancel();
            //System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+pattern);
            if(pattern.equals("10101"))
            {
                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$Matched");
                Match=true;
                promptSpeechInput();

            }

            else
            {
            }

            pattern="";
            running=false;
        }
    }

*/










    /*
    void init()
    {
        for(int i=0; i < 400; i++)
        {
            contactName[i] = contactNumber[i] = "";
        }
    }
    */

    /*

        Text to Speech

        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

     */

    // Function to activate speech input
    private void promptSpeechInput1(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @RequiresApi(api = Build.VERSION_CODES.M)

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    // txtSpeechInput.setText(result.get(0));
                    String s = result.get(0);
                    String a[] = s.split(" ");
                    String destination = " ";
                    for (String anA : a) {
                        destination = destination + anA + "+";
                    }
                    destination = destination.substring(0, destination.length() - 1);
                    navText.setText(destination);
                    destination = "google.navigation:q=" + destination + "&mode=w";
                    try {
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(destination));
                        startActivity(intent);
                    } catch (ActivityNotFoundException noMaps) {
                        String speakText = getResources().getString(R.string.no_map);
                        Toast.makeText(this, speakText, Toast.LENGTH_LONG).show();
                        speech.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
                break;
            }
        }
    }

}
