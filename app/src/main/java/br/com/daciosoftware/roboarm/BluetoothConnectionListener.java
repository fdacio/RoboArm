package br.com.daciosoftware.roboarm;

import android.bluetooth.BluetoothDevice;

public interface BluetoothConnectionListener {
    void setConnected(BluetoothDevice device);
    void setDisconnected();
    void readFromDevicePaired(String dadosLido);
}
