package com.example.myapplication;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    Connection conn;
    android.bluetooth.BluetoothAdapter bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
    ArrayList<IdMacaddressPair> idMacaddressPairs = new ArrayList<IdMacaddressPair>();
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

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if(conn == null){
            ConnectionDb();
        }
        if(idMacaddressPairs.size() == 0 && conn != null){
            getEventList();
        }
        else{
            Context context = getApplicationContext();
            CharSequence text = "No db connection";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        if (!bluetoothAdapter.isEnabled()) {
            android.content.Intent enableBtIntent;
            enableBtIntent = new android.content.Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }
        else{
            bluetoothAdapter.startDiscovery();
        }

        if (bluetoothAdapter == null) {
            Context context = getApplicationContext();
            CharSequence text = "Hello toastt!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }








    private final android.content.BroadcastReceiver receiver = new android.content.BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                String deviceHardwareAddress = device.getAddress(); // MAC address
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


                for (Devicepair d : items) {
                    if(d.device.equals("94:B1:0A:BA:35:A2")){
                        output += "hallo ik ben wanneee";
                    }
                    for (int i = 0; i < idMacaddressPairs.size(); i++) {
                        if (d.device.equals(idMacaddressPairs.get(i).macaddress) && rssi > -80) {
                            checkInById(idMacaddressPairs.get(i).id);

                            output += "Id " + idMacaddressPairs.get(i).id + " has been checked in!: " + rssi;
                            idMacaddressPairs.remove(i);
                        }
                    }
                }




                android.widget.TextView textView = findViewById(R.id.editText);

                textView.setText(output);
                //textView.setText(textView.getText() + "\n " + "\n" +  deviceHardwareName+ " "  +deviceHardwareAddress + " " + rssi);
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                new android.os.CountDownTimer(5000, 1000) {
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

    public void ConnectionDb(){
        try{
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://145.24.222.158:5432/label_a?user=postgres&password=ww123");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void getEventList(){
        Statement st = null;
        try {
            st = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResultSet result;
        String sql;
        sql = "select user_events.id,users.mac_address from user_events LEFT JOIN users ON user_events.id=users.id WHERE users.mac_address IS NOT NUll AND user_events.bluetooth = false";
        try {
            result = st.executeQuery(sql);
            while(result.next()) {
                idMacaddressPairs.add(new IdMacaddressPair(result.getInt(1), result.getString(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkInById(Integer id){
        String sql;
        PreparedStatement st = null;

        sql = "UPDATE user_events SET bluetooth = true, bluetooth_timestamp = current_timestamp where user_id = " + id.toString();
        try {
            st = conn.prepareStatement(sql);
            int count = st.executeUpdate();
            if(count == 1){
                return true;
            }
            else{
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


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
