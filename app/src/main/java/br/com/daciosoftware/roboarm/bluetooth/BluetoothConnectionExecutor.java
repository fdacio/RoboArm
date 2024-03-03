package br.com.daciosoftware.roboarm.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BluetoothConnectionExecutor {

    private final BluetoothManagerControl mmBluetoothManagerControl;
    private boolean connected = false;
    private BluetoothSocket bluetoothSocket;
    private OutputStream mmOutStream;
    private InputStream mmInputStream;

    protected BluetoothConnectionExecutor(BluetoothManagerControl bluetoothManagerControl) {
        mmBluetoothManagerControl = bluetoothManagerControl;
    }

    @SuppressLint("MissingPermission")
    public void executeConnection(BluetoothDevice device) {
        Handler handlerConnection = new Handler(Looper.getMainLooper());
        ExecutorService executorConnection = Executors.newSingleThreadExecutor();
        try {
            executorConnection.execute(() -> {
                BluetoothSocket tmp;
                String _uuid = "00001101-0000-1000-8000-00805F9B34FB";
                try {
                    tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(_uuid));
                    bluetoothSocket = tmp;
                    OutputStream tmpOut = null;
                    InputStream tmpIn = null;
                    if (bluetoothSocket != null) {
                        tmpOut = bluetoothSocket.getOutputStream();
                        tmpIn = bluetoothSocket.getInputStream();
                    }
                    mmOutStream = tmpOut;
                    mmInputStream = tmpIn;
                    //Metodo é bloqueante por 12 segundos de timeout
                    bluetoothSocket.connect();
                    connected = bluetoothSocket.isConnected();
                } catch (IOException e) {
                    connected = false;
                }
                //Aqui seta a conexao
                handlerConnection.post(() -> {
                    if (connected) {
                        mmBluetoothManagerControl.setDevicePaired(device);
                        mmBluetoothManagerControl.getListenerConnectionDevice().postDeviceConnection(device);
                        new BluetoothConnectionDataReceived().executeDataReceived();
                    } else {
                        mmBluetoothManagerControl.getListenerConnectionDevice().postFailConnection(device);
                    }
                });
            });
            executorConnection.shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (executorConnection != null) {
                executorConnection.shutdownNow();
            }
        }
    }


    public void executeDisconnect() {
        if (!connected) return;
        ExecutorService executorDisconnect = Executors.newSingleThreadExecutor();
        try {
            Handler handlerDisconnect = new Handler(Looper.getMainLooper());
            executorDisconnect.execute(() -> {
                disconnect();
                handlerDisconnect.post(() -> {
                    mmBluetoothManagerControl.setDevicePaired(null);
                    mmBluetoothManagerControl.getListenerConnectionDevice().postDeviceDisconnection();
                });
            });
            executorDisconnect.shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (executorDisconnect != null) {
                executorDisconnect.shutdownNow();
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            if (mmOutStream != null) {
                mmOutStream.write(buffer);
            }
        } catch (IOException e) {
            connected = false;
            e.printStackTrace();
        }
    }

    private void closeSocketAndStream() {
        try {
            if (mmOutStream != null) {
                mmOutStream.close();
                mmOutStream = null;
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        connected = false;
        closeSocketAndStream();
    }

    private class BluetoothConnectionDataReceived {
        StringBuilder dataReceived;

        public void executeDataReceived() {
            ExecutorService executorDataReceived = null;
            try {
                executorDataReceived = Executors.newSingleThreadExecutor();
                Handler handlerDataReceived = new Handler(Looper.getMainLooper());
                executorDataReceived.execute(() -> {
                    while (connected) {
                        if (mmInputStream != null) {
                            try {
                                byte[] buffer = new byte[1024];
                                int byteLidos = mmInputStream.read(buffer);
                                if (byteLidos > 0) {
                                    dataReceived = new StringBuilder();
                                    for (int i = 0; i < 1024; i++) {
                                        if ((buffer[i] != '\n') && (buffer[i] != '\r')) {
                                            dataReceived.append((char) buffer[i]);
                                        } else {
                                            break;
                                        }
                                    }
                                    if (dataReceived.length() > 0) {
                                        handlerDataReceived.post(() -> mmBluetoothManagerControl.getListenerConnectionDevice().postDataReceived(dataReceived.toString()));
                                    }
                                }
                            } catch (IOException e) {
                                break;
                            }
                        }
                    }

                    executeDisconnect();

                });
                executorDataReceived.shutdown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (executorDataReceived != null) {
                    executorDataReceived.shutdownNow();
                }
            }
        }
    }

    @Deprecated
    private class BluetoothConnectionListenerServer implements Runnable {
        @Override
        public void run() {
            while (connected) {
                try {
                    if (mmInputStream != null) {
                        byte[] buffer = new byte[1024];
                        int byteLidos = mmInputStream.read(buffer);
                        if (byteLidos > 0) {
                            StringBuilder leitura = new StringBuilder();
                            for (int i = 0; i < 1024; i++) {
                                if ((buffer[i] != '\n') && (buffer[i] != '\r')) {
                                    leitura.append((char) buffer[i]);
                                } else {
                                    if (leitura.length() > 0) {
                                        mmBluetoothManagerControl.getListenerConnectionDevice().postDataReceived(leitura.toString());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }
            executeDisconnect();
        }
    }

}
