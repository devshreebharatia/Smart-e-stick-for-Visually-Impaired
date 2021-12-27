package com.devansh.smartstick;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Permission extends AppCompatActivity {

    TextView txt;
    Button btn;
    String requiredPermission;
    boolean response=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        this.txt = (TextView) findViewById(R.id.txt);
        this.btn = (Button) findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(btn.getText().toString().equalsIgnoreCase("next"))
                {

                    Intent intent = new Intent(Permission.this,MainActivity.class);
                    finish();
                    startActivity(intent);

                }
                else
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{requiredPermission}, 105);
                    }
                    response=true;

                }

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if ((checkCallingOrSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED)||(checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED)||(checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED)||(checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED)||(checkCallingOrSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED)||(checkCallingOrSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED)||(checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED)||(checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)||(checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)) {


                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        permissionCheck();
                    }
                }).start();

            }
            else
            {

                Intent intent = new Intent(Permission.this,MainActivity.class);
                finish();
                startActivity(intent);

            }
        }

    }

    public void permissionCheck()
    {

        while(true) {
            if(response) {


                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                    if (checkCallingOrSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
                        requiredPermission = Manifest.permission.CALL_PHONE;
                        txt.setText("Call_Phone Permission");
                        btn.setText("Allow");
                        response=false;
                        //requestPermissions(new String[]{requiredPermission}, 105);
                    }
                    else if (checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED) {
                        requiredPermission = Manifest.permission.BLUETOOTH;
                        txt.setText("Bluetooth Permission");
                        btn.setText("Allow");
                        response=false;
                    }
                    else if (checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED) {
                        requiredPermission = Manifest.permission.BLUETOOTH_ADMIN;
                        txt.setText("Bluetooth_Admin Permission");
                        btn.setText("Allow");
                        response=false;
                    }
                    else if (checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                        requiredPermission = Manifest.permission.RECORD_AUDIO;
                        txt.setText("Record_Audio Permission");
                        btn.setText("Allow");
                        response=false;
                    }
                    else if (checkCallingOrSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
                        requiredPermission = Manifest.permission.READ_CONTACTS;
                        txt.setText("Read_Contacts Permission");
                        btn.setText("Allow");
                        response=false;
                    }
                    else if (checkCallingOrSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
                        requiredPermission = Manifest.permission.SEND_SMS;
                        txt.setText("Send_SMS Permission");
                        btn.setText("Allow");
                        response=false;
                    }
                    else if (checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
                        requiredPermission = Manifest.permission.READ_PHONE_STATE;
                        txt.setText("Read_Phone_State Permission");
                        btn.setText("Allow");
                        response=false;
                    }
                    else if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                        requiredPermission = Manifest.permission.ACCESS_FINE_LOCATION;
                        txt.setText("Access_Fine_Location Permission");
                        btn.setText("Allow");
                        response=false;
                    }
                    else if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                        requiredPermission = Manifest.permission.ACCESS_COARSE_LOCATION;
                        txt.setText("Access_Course_Location Permission");
                        btn.setText("Allow");
                        response=false;
                    }
                    else {
                        txt.setText("");
                        btn.setText("Next");
                    }


                }
            }
        }

    }

}
