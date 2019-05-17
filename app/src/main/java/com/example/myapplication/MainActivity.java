package com.example.myapplication;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    android.bluetooth.BluetoothAdapter bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
    ArrayList<Devicepair> items = new ArrayList<Devicepair>();
    String output = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        android.content.IntentFilter filter = new android.content.IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        filter = new android.content.IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);


        if (bluetoothAdapter == null) {
            Context context = getApplicationContext();
            CharSequence text = "Hello toastt!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            android.content.Intent enableBtIntent;
            enableBtIntent = new android.content.Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

    }









    private final android.content.BroadcastReceiver receiver = new android.content.BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String deviceHardwareAddress = device.getAddress(); // MAC address
                String deviceHardwareName = device.getName(); // Name
                Boolean added = false;


                for (Devicepair d: items) {
                    if(d.device.equals(deviceHardwareAddress)){
                        d.rssi = rssi;
                        added =true;
                    }
                }
                if (added == false){
                    items.add(new Devicepair(rssi, deviceHardwareAddress));
                }



                android.widget.TextView textView = findViewById(R.id.editText);

                for (Devicepair d: items) {
                    output += "Mac: " + d.device + " RSSI: " + d.rssi + "       ";
                }
                textView.setText("");
                textView.setText(output);
                output = "";
                //textView.setText(textView.getText() + "\n " + "\n" +  deviceHardwareName+ " "  +deviceHardwareAddress + " " + rssi);
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                new android.os.CountDownTimer(15000, 1000) {
                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        Context context = getApplicationContext();
                        CharSequence text = "letsgoo";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        bluetoothAdapter.cancelDiscovery();
                        bluetoothAdapter.startDiscovery();

                    }
                }.start();
            }

        }
    };



    protected void onActivityResult (int requestCode,int resultCode, Intent data){
        if(resultCode == -1) {
            Context context = getApplicationContext();
            CharSequence text = "letssgoooo";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            bluetoothAdapter.startDiscovery();

        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }
}
