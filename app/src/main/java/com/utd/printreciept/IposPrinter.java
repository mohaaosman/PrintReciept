package com.utd.printreciept;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IposPrinter {
    private Context context;
    private Activity activity;
    private BluetoothSocket socket;
    private BluetoothDevice bluetoothDevice;
    private OutputStream outputStream;
    private InputStream inputStream;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;
    private String value = "";


    public IposPrinter(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void InitPrinter() {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("IposPrinter")) //Note, you will need to change this to match the name of your device
                    {
                        bluetoothDevice = device;
                        break;
                    }
                }

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID

                Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", int.class);

                socket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);

                bluetoothAdapter.cancelDiscovery();

                socket.connect();

                outputStream = socket.getOutputStream();

                inputStream = socket.getInputStream();

                beginListenForData();
            } else {
                value += "No Devices found";
                Toast.makeText(context, value, Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            value += ex.toString() + "\n" + " InitPrinter \n";
            Toast.makeText(context, value, Toast.LENGTH_LONG).show();
        }
    }

    private void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            // specify US-ASCII encoding
            // tell the user data were sent to bluetooth printer device
            Thread workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = inputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                final int read = inputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, StandardCharsets.US_ASCII);

                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Log.d("e", data);
                                            }
                                        });

                                    } else {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param txtvalue the text to be printed
     */
    public void IntentPrint(String txtvalue) {
        byte[] buffer = txtvalue.getBytes();

        byte[] PrintHeader;

        Toast.makeText(context, "Printing..", Toast.LENGTH_LONG).show();

        PrintHeader = new byte[]{(byte) 0xAA, 0x55, 2, 0};

        PrintHeader[3] = (byte) buffer.length;

        InitPrinter();

        if (PrintHeader.length > 128) {
            value += "\nValue is more than 128 size\n";
            Toast.makeText(context, value, Toast.LENGTH_LONG).show();
            return;
        }


        try {

            outputStream.write(txtvalue.getBytes());

            outputStream.close();
            socket.close();
        } catch (Exception ex) {
            value = "";
            value += "\n" + "Exception IntentPrint \n" + ex.toString();
            Log.v("PrinterException", value);
            Toast.makeText(context, value, Toast.LENGTH_LONG).show();
        }
    }

    public void GetConnectedDevices() {
        BluetoothAdapter mBluetoothAdapter;
        Set<BluetoothDevice> pairedDevices;
        List<String> s;
        StableArrayAdapter adapter;

//        DevicesList = findViewById(R.id.deviceslv);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        s = new ArrayList<>();

        for (BluetoothDevice bt : pairedDevices)
            s.add(bt.getName());

        Toast.makeText(context, s.get(0), Toast.LENGTH_LONG).show();


//        adapter = new StableArrayAdapter(this,
//                android.R.layout.simple_list_item_1, s);
//
//
//
//        DevicesList.setAdapter(adapter);

    }

}
