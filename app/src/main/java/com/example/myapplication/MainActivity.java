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


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Connection conn;
    ArrayList<String> macAddressList = new ArrayList<String>();
    ArrayList<Integer> userEventIdList = new ArrayList<Integer>();
    android.bluetooth.BluetoothAdapter bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
    ArrayList<Devicepair> items = new ArrayList<Devicepair>();
    ArrayList<IdRssiPair> Idrssi = new ArrayList<IdRssiPair>();
    String output = "";
    Spinner spinner;
    String scanner = "s1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(R.layout.choose_scanner);

        spinner = findViewById(R.id.choose_scanner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.scanners, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Button button = (Button) findViewById(R.id.choose_scanner_btn);
        button.setOnClickListener(this);




        android.content.IntentFilter filter = new android.content.IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        filter = new android.content.IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if(conn == null){
            ConnectionDb();
        }
        if(macAddressList.size() == 0){
            getEventList();
        }

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
                    if(d.device.equals("18:F0:E4:F4:3D:B8")){
                        output += "Mac: " + d.device + " RSSI: " + d.rssi + "       ";
                    }
                    //output += "Mac: " + d.device + " RSSI: " + d.rssi + "       ";

                    for (int y = 0; y < macAddressList.size(); y++) {
                        if(macAddressList.get(y).equals(d.device)){


                            Idrssi.add(new IdRssiPair(userEventIdList.get(y), d.rssi));
                            // output += "Mac: " + d.device + " RSSI: " + d.rssi + "       ";
                        }
                    }
                }

                if(!Idrssi.isEmpty()){
                    boolean result = setCheckedInByMacAddress(Idrssi, scanner);
                    if(!result){
                        CharSequence text = "deze dingetje is kaput";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                    //output += "A person checked in!";
                }
                else{
                    //output += "no one found to check in";
                }

                textView.setText("");
                textView.setText(output);
                output = "";
                Idrssi.clear();
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

                        setCheckedIn();
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
        sql = "select user_events.id,users.mac_address from user_events LEFT JOIN users ON user_events.id=users.id WHERE users.mac_address IS NOT NUll";
        try {
            result = st.executeQuery(sql);
            while(result.next()) {
                macAddressList.add(result.getString(2));
                userEventIdList.add(result.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean setCheckedInByMacAddress(ArrayList<IdRssiPair> idList, String scanner){
        String sql;
        PreparedStatement st = null;

        for (IdRssiPair d: idList) {
            sql = "UPDATE user_events SET "+scanner+" = " + d.rssi + " where id = " + d.id;
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
        }
        return false;
    }

    public boolean setCheckedIn(){
        String sql;
        Statement st = null;
        try {
            st = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sql = "select id,s1,s2,s3 from user_events where checked_in IS NOT true";
        ResultSet result;
        ArrayList<Integer> idList = new ArrayList<Integer>();
        try {
            result = st.executeQuery(sql);
            if(!result.next()){
                return false;
            }
            while(result.next()) {
                if(result.getInt(2) != 0 && result.getInt(3) != 0 && result.getInt(4) != 0){
                    idList.add(result.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        String idListString = "";

        for (int i = 0; i < idList.size(); i++) {
            if(idList.size()-1 == i){
                idListString += idList.get(i);
            }
            else{
                idListString += idList.get(i)+ ",";
            }
        }
        sql = "UPDATE user_events SET checked_in = TRUE, checked_in_at = CURRENT_TIMESTAMP WHERE id IN ("+idListString+")";

        try {
            PreparedStatement st2 = null;
            st2 = conn.prepareStatement(sql);
            int count = st2.executeUpdate();
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

    @Override
    public void onClick(View view) {
        String option = spinner.getSelectedItem().toString();
        if(option.equals("Scanner 3")){
            scanner = "s1";
        }
        else if(option.equals("Scanner 2")){
            scanner = "s2";
        }
        else{
            scanner = "s1";
        }

        bluetoothAdapter.startDiscovery();
        setContentView(R.layout.activity_main);
    }
}
