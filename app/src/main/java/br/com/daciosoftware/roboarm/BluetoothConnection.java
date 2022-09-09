package br.com.daciosoftware.roboarm;

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

public class BluetoothConnection extends AsyncTask<Void, Void, BluetoothDevice> {

    private BluetoothSocket mmSocket;
    private OutputStream mmOutStream;
    private InputStream mmInputStream;
    private boolean connected = false;
    private BluetoothDevice mmDevice;
    private BluetoothConnectionListener mmListener;
    private Context mmContext;
    private ProgressDialog progressDialog;
    private char[] dados;

    public BluetoothConnection(BluetoothDevice device, BluetoothConnectionListener listener, Context context) {

        mmDevice = device;
        mmListener = listener;
        mmContext = context;
    }

    public BluetoothConnectionListener getListener() {
        return mmListener;
    }

    @Override
    protected BluetoothDevice doInBackground(Void... params) {
        BluetoothSocket tmp = null;
        String _uuid  = "9863bf68-8b8f-11ec-a8a3-0242ac120002";
        String _uuid2 = "00001101-0000-1000-8000-00805F9B34FB";
        try {
            tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(_uuid2));

        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
        OutputStream tmpOut = null;
        InputStream tmpIn = null;
        try {
            tmpOut = mmSocket.getOutputStream();
            tmpIn = mmSocket.getInputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }
        mmOutStream = tmpOut;
        mmInputStream = tmpIn;

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        try {
            mmSocket.connect();
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
            try {
                mmSocket.close();
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
            Thread bluetoothConnectionListenerServer = new Thread(new BluetoothConnectionListenerServer());
            bluetoothConnectionListenerServer.start();
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
        try {
            if (mmOutStream != null) {
                mmOutStream.close();
                mmOutStream = null;
            }
            if (mmSocket != null) {
                mmSocket.close();
                mmSocket = null;
            }
            connected = false;
            mmListener.setDisconnected();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    //Class thread escuta dados do servidor bluetooth
    private class BluetoothConnectionListenerServer implements Runnable {
        @Override
        public void run() {
            /*
            while(true) {
                try {
                    byte[] buffer = new byte[1024];
                    mmInputStream.read(buffer);
                    mmListener.listenerServer(buffer);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
             */
        }
    }
}
