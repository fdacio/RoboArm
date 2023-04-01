package br.com.daciosoftware.roboarm;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnectionTask extends AsyncTask<Void, Void, BluetoothDevice> {

    private BluetoothSocket mmBluetoothSocket;
    private OutputStream mmOutStream;
    private InputStream mmInputStream;
    private boolean connected = false;
    private BluetoothDevice mmDevice;
    private BluetoothConnectionListener mmListener;
    private Context mmContext;
    private ProgressDialog progressDialog;
    private Thread threadPairedDeviceObserver;

    public BluetoothConnectionTask(BluetoothDevice device, BluetoothConnectionListener listener, Context context) {

        mmDevice = device;
        mmListener = listener;
        mmContext = context;
    }

    public BluetoothConnectionListener getListener() {
        return mmListener;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected BluetoothDevice doInBackground(Void... params) {
        BluetoothSocket bluetoothSocket = null;
        String _uuid2 = "00001101-0000-1000-8000-00805F9B34FB";
        try {
            bluetoothSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(_uuid2));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmBluetoothSocket = bluetoothSocket;
        OutputStream tmpOut = null;
        InputStream tmpIn = null;
        try {
            tmpOut = mmBluetoothSocket.getOutputStream();
            tmpIn = mmBluetoothSocket.getInputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }
        mmOutStream = tmpOut;
        mmInputStream = tmpIn;

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        try {
            mmBluetoothSocket.connect();
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
            try {
                mmBluetoothSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return mmDevice;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(mmContext, "Bluetooth", "Aguarde, pareando ...");
    }

    @Override
    protected void onPostExecute(BluetoothDevice device) {
        progressDialog.dismiss();
        if (connected) {
            mmListener.setConnected(device);
            threadPairedDeviceObserver = new Thread(new ThreadPairedDeviceObserver());
            threadPairedDeviceObserver.start();

        } else {
            Toast.makeText(mmContext, "Não foi possível conectar.", Toast.LENGTH_LONG).show();
        }
    }

    public void
    write(byte[] buffer) {
        try {
            if (mmOutStream != null) {
                mmOutStream.write(buffer);
            }
        } catch (IOException e) {
            connected = false;
            e.printStackTrace();
        }
    }

    public void disconnect() {
        threadPairedDeviceObserver.interrupt();
        connected = false;
        mmListener.setDisconnected();
        if (mmBluetoothSocket != null) {
            try {
                mmBluetoothSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    //Class thread escuta dados do dispositivo ṕareado bluetooth
    private class ThreadPairedDeviceObserver implements Runnable {
        @Override
        public void run() {
            while (connected) {
                if (mmInputStream != null){
                    try {
                        byte[] buffer = new byte[1024];
                        mmInputStream.read(buffer);
                        StringBuilder leitura = new StringBuilder();
                        for (int i = 0; i < 1024; i++) {
                            if ((buffer[i] != '\n') && (buffer[i] != '\r')) {
                                leitura.append((char) buffer[i]);
                            } else {
                                if (leitura.length() > 0) {
                                    mmListener.readFromDevicePaired(leitura.toString());
                                }
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }
    }
}
