package br.com.daciosoftware.roboarm.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class BluetoothManagerControl {

    private static final int REQUEST_PERMISSION_BLUETOOTH = 2;
    private final static int REQUEST_ENABLE_BLUETOOTH = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice devicePaired;
    private BluetoothBroadcastReceive bluetoothBroadcastReceiver;
    private DiscoveryDevices listenerDiscoveryDevices;
    private ConnectionDevice listenerConnection;
    private final Context appContext;

    private static BluetoothManagerControl bluetoothManagerControl;

    private final BluetoothConnectionExecutor bluetoothConnectionExecutor;

    private BluetoothManagerControl(Context context) {
        appContext = context;
        bluetoothConnectionExecutor = new BluetoothConnectionExecutor(this);
    }

    public static BluetoothManagerControl getInstance(Context context) {
        if (bluetoothManagerControl == null) {
            bluetoothManagerControl = new BluetoothManagerControl(context);
        }
        return bluetoothManagerControl;
    }

    public void registerBluetoothBroadcastReceive() {
        bluetoothBroadcastReceiver = new BluetoothBroadcastReceive(appContext, listenerDiscoveryDevices);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        appContext.registerReceiver(bluetoothBroadcastReceiver, filter);
    }

    public void unregisterBluetoothBroadcastReceive() {
        appContext.unregisterReceiver(bluetoothBroadcastReceiver);
    }

    public DiscoveryDevices getListenerDiscoveryDevice() {
        return this.listenerDiscoveryDevices;
    }

    public void setListenerDiscoveryDevices(DiscoveryDevices listenerDiscoveryDevices) {
        this.listenerDiscoveryDevices = listenerDiscoveryDevices;
    }

    public ConnectionDevice getListenerConnectionDevice() {
        return this.listenerConnection;
    }

    public void setListenerConnectionDevice(ConnectionDevice listenerConnection) {
        this.listenerConnection = listenerConnection;
    }

    @SuppressLint("MissingPermission")
    public void initDiscovery() {
        if (!checkPermissionAccessLocation()) {
            requestPermissionAccessLocation();
            return;
        }
        if (!checkBluetoothPermissionScan()) {
            requestPermissionBluetooth();
            return;
        }
        if (!checkBluetoothAdapterEnable()) {
            requestEnableBluetoothAdapter();
            return;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) appContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothManager.getAdapter().startDiscovery();
    }

    public void connect(BluetoothDevice device) {

        if (!checkPermissionAccessLocation()) {
            requestPermissionAccessLocation();
            return;
        }
        if (!checkBluetoothPermissionScan()) {
            requestPermissionBluetooth();
            return;
        }
        if (!checkBluetoothAdapterEnable()) {
            requestEnableBluetoothAdapter();
            return;
        }

        listenerConnection.initConnection();
        bluetoothConnectionExecutor.executeConnection(device);

    }

    public void disconnect() {
       bluetoothConnectionExecutor.executeDisconnect();
    }

    public void write(byte[] dados) {
        bluetoothConnectionExecutor.write(dados);
    }

    public BluetoothDevice getDevicePaired() {
        return devicePaired;
    }

    public void setDevicePaired(BluetoothDevice devicePaired) {
        this.devicePaired = devicePaired;
    }

    @SuppressLint("MissingPermission")
    public List<BluetoothDevice> getBoundedDevices() {
        List<BluetoothDevice> listDevices = new ArrayList<>();
        if (checkBlutoothPermissionConnect()) {
            BluetoothManager bluetoothManager = (BluetoothManager) appContext.getSystemService(Context.BLUETOOTH_SERVICE);
            listDevices.addAll(bluetoothManager.getAdapter().getBondedDevices());
        }
        return listDevices;
    }

    public void requestPermissionBluetooth() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions((Activity) appContext, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN}, REQUEST_PERMISSION_BLUETOOTH);

        } else {

            ActivityCompat.requestPermissions((Activity) appContext, new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_BLUETOOTH);

        }
    }

    public void requestPermissionAccessLocation() {
        ActivityCompat.requestPermissions((Activity) appContext, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_BLUETOOTH);
    }

    @SuppressLint("MissingPermission")
    public void requestEnableBluetoothAdapter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int permissionBluetoothConnect = ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT);
            if (permissionBluetoothConnect != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        BluetoothManager bluetoothManager = (BluetoothManager) appContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) appContext).startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    public boolean checkBluetoothAdapterEnable() {
        BluetoothManager bluetoothManager = (BluetoothManager) appContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        return (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
    }

    public boolean checkBluetoothPermissionScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int permissionBluetoothScan = ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_SCAN);
            return (permissionBluetoothScan == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    public boolean checkBlutoothPermissionConnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int permissionBluetoothConnect = ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT);
            return (permissionBluetoothConnect == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    public boolean checkPermissionAccessLocation() {
        int permissionCoarseLocation = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionFineLocation = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION);
        return ((permissionCoarseLocation == PackageManager.PERMISSION_GRANTED) && (permissionFineLocation == PackageManager.PERMISSION_GRANTED));

    }

    public interface DiscoveryDevices {
        void initDiscoveryDevices();

        void finishDiscoveryDevices();

        void foundDevice(BluetoothDevice device);
    }

    public interface ConnectionDevice {
        void initConnection();
        void postDeviceConnection();
        void postDeviceDisconnection();
        void postFailConnection();
        void postDataReceived(String dataReceived);
    }

}
