package com.imperialigem.will.pixeldraw;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import android.widget.Button;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private final String DEVICE_NAME="DSD TECH HC-05";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //Serial Port Service ID
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private OutputStream outputStream = null;
    private InputStream inputStream;
    Button doneButton;
    boolean deviceConnected=false;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    volatile boolean stopWorker;

    private PCanvas pCanvas;
    private BluetoothAdapter bluetoothAdapter;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doneButton = (Button) findViewById(R.id.doneButton);
        doneButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDone();
            }
        });
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.action_paintbrush:
                                pCanvas.setBrush();
                                break;
                            case R.id.action_eraser:
                                pCanvas.setEraser();
                                break;
                            case R.id.action_clear:
                                pCanvas.clear();
                                pCanvas.brushOff();
                                bottomNavigationView.setSelectedItemId(R.id.item_none);
                                break;
                            case R.id.action_selectpattern:
                                showPager();
                                bottomNavigationView.setSelectedItemId(R.id.item_none);
                                break;
                        }
                        return true;
                    }
                });
        pCanvas = (PCanvas) findViewById(R.id.pCanvas);
        pCanvas.init(metrics);
        if(!BTInit()){
            BTInitFailAlert();
        }
        else {
            if (!BTConnect()) {
                BTConnectFailAlert();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelConnection();

    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        bufferPosition = 0;
        buffer = new byte[1024];
        thread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = inputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[bufferPosition];
                                    System.arraycopy(buffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    bufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            if (data == "resend_data"){
                                                onConfirmSend();
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    buffer[bufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        thread.start();
    }

    public boolean BTConnect()
    {
        deviceConnected=true;
        bluetoothAdapter.cancelDiscovery();
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            deviceConnected=false;
        }

        if(deviceConnected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
                beginListenForData();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return deviceConnected;
    }

    public void BTConnectFailAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Bluetooth Connection Fail");
        alertDialog.setMessage("Connection to electrochemical array failed");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    public boolean BTInit()
    {
        boolean found=false;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesn't Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getName().equals(DEVICE_NAME))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    }

    public void BTInitFailAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Bluetooth Initialisation Fail");
        alertDialog.setMessage("404: Electrochemical array not found");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void BTSendFailAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Data Send Failed");
        alertDialog.setMessage("The data could not be sent to the array");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
    public void sendAlert(String s){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("String:");
        alertDialog.setMessage(s);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    public int[][] getMatrix(String[] arr){
        int[][] result = new int[10][10];
        for(int i = 0; i < 10; i++){
            for(int j = 0; j <10; j++){
                result[j][i] = Character.getNumericValue(arr[i].charAt(j));
            }
        }
        return result;
    }

    public void cancelConnection() {
        try {
            socket.close();
        } catch (IOException e) { }
    }

    private void confirmSendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm send");
        builder.setMessage("Are you sure you have finished the pattern and wish to send it?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onConfirmSend();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void onClickDone(){
        confirmSendDialog();
    }

//still needs to be implemented properly
    public void onConfirmSend() {
        if(BTInit()) {
            if(!deviceConnected) {
                if(BTConnect()) {
                    String string = pCanvas.sendData();
                    byte[] msgBuffer = string.getBytes();
                    try {
                        outputStream.write(msgBuffer);
                        sendAlert(string);
                    } catch (IOException e) {
                        e.printStackTrace();
                        BTSendFailAlert();
                    }
                }
                else{
                    BTConnectFailAlert();
                }
            }
            else{
                String string = pCanvas.sendData();
                byte[] msgBuffer = string.getBytes();
                try {
                    outputStream.write(msgBuffer);
                    sendAlert(string);
                } catch (IOException e) {
                    e.printStackTrace();
                    BTSendFailAlert();
                }
            }

        }
        else{
            BTInitFailAlert();
        }
    }

    public void showPager(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pager_layout);



        final ViewPager pager = (ViewPager) dialog.findViewById(R.id.pager);
        final CustomPagerAdapter customPagerAdapter = new CustomPagerAdapter(this);
        pager.setAdapter(customPagerAdapter);

        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button selectColorButton = dialog.findViewById(R.id.setColorButton);
        selectColorButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomPagerEnum currentEnum = CustomPagerEnum.values()[pager.getCurrentItem()];
                pCanvas.setColor(getResources().getColor(currentEnum.getColorResId()));
                dialog.dismiss();
            }
        });

        Button selectButton = dialog.findViewById(R.id.selectButton);
        selectButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomPagerEnum currentEnum = CustomPagerEnum.values()[pager.getCurrentItem()];
                pCanvas.setPattern(getMatrix(getResources().getStringArray(currentEnum.getPatternResId())),getResources().getColor(currentEnum.getColorResId()));
                pCanvas.brushOff();
                dialog.dismiss();
            }
        });
        /*BottomNavigationView patternselectbar;
        patternselectbar = (BottomNavigationView) dialog.findViewById(R.id.patternselectbar);
        patternselectbar.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        patternselectbar.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        CustomPagerEnum currentEnum = CustomPagerEnum.values()[pager.getCurrentItem()];
                        switch (item.getItemId()) {
                            case R.id.action_selectpattern:
                                pCanvas.setPattern(getMatrix(getResources().getStringArray(currentEnum.getPatternResId())),getResources().getColor(currentEnum.getColorResId()));
                                pCanvas.brushOff();
                                dialog.dismiss();
                                break;
                            case R.id.action_setcolor:
                                pCanvas.setColor(getResources().getColor(currentEnum.getColorResId()));
                                dialog.dismiss();
                                break;
                            case R.id.action_cancelpatternselect:
                                dialog.dismiss();
                                break;

                        }
                        return true;
                    }
                });*/
        dialog.show();
    }

}
