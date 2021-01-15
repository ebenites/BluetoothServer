package com.nextmedicall.bluetoothserver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.UUID;

public class AcceptThread extends Thread {

    private static final String TAG = AcceptThread.class.getSimpleName();

    private static final UUID UDDI_MODULE_BT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothServerSocket mmServerSocket;

    private final TextView messageText;

    final int messageState = 0; //Estado del manejador

    private final Handler messagegHandler = new Handler(){
        public void handleMessage(Message msg) {
            if (msg.what == messageState) {
                String message = (String) msg.obj;
                messageText.setText("Mensaje: " + message);
            }
        }

    };    //Handler es un control para mensajes

    public AcceptThread(TextView messageText) {
        this.messageText = messageText;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("My Bluetooth Server", UDDI_MODULE_BT);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            Log.d(TAG, "Server accepting... ");

            try {
                messagegHandler.obtainMessage(messageState, "Esperando conexión...").sendToTarget();
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
//                manageMyConnectedSocket(socket);

                try (OutputStream out = socket.getOutputStream()) {

                    while (true) {

                        double temp = (new Random().nextDouble() * 20) + 10;
                        double tempmin = temp - (new Random().nextDouble() * 5);
                        double tempmax = temp + (new Random().nextDouble() * 5);

                        double bpm = (new Random().nextDouble() * 100) + 1;
                        double bpmmin = bpm - (new Random().nextDouble() * 5);
                        double bpmmax = bpm + (new Random().nextDouble() * 5);

                        double spo2 = (new Random().nextDouble() * 100) + 1;
                        double spo2min = spo2 - (new Random().nextDouble() * 5);
                        double spo2max = spo2 + (new Random().nextDouble() * 5);

                        String data = String.format("Temp: min=%.2f,max=%.2f,ave=%.2f\nBPM: min=%.2f,max=%.2f,ave=%.2f\nSPO2: min=%.2f,max=%.2f,ave=%.2f\n",
                                tempmin, tempmax, temp, bpmmin, bpmmax, bpm, spo2min, spo2max, spo2);
                        Log.d(TAG, "output: " + data);
                        messagegHandler.obtainMessage(messageState, data).sendToTarget();

                        out.write(data.getBytes());

                        Thread.sleep(1000);

                    }

                } catch (IOException | InterruptedException e) {
                    Log.e(TAG, "Error on communication", e);
                }

                // Detiene el servidor
//                try {
//                    mmServerSocket.close();
//                } catch (IOException e) {
//                    Log.e(TAG, "Could not close the connect socket", e);
//                }
//                break;
            }
        }
    }

}
