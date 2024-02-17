package br.com.daciosoftware.roboarm.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

public class BluetoothBroadcastReceive extends BroadcastReceiver {

    private final Context appContext;
    private final BluetoothManagerControl.DiscoveryDevices listenerDiscoveryDevices;

    public BluetoothBroadcastReceive (Context context, @NonNull BluetoothManagerControl.DiscoveryDevices listenerDiscoveryDevices ) {
        this.appContext = context;
        this.listenerDiscoveryDevices = listenerDiscoveryDevices;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            listenerDiscoveryDevices.initDiscoveryDevices();
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            listenerDiscoveryDevices.finishDiscoveryDevices();
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device =  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            listenerDiscoveryDevices.foundDevice(device);
        }
    }

}
