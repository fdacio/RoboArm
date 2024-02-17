package br.com.daciosoftware.roboarm.ui.bluetotooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import br.com.daciosoftware.roboarm.R;
import br.com.daciosoftware.roboarm.alertdialog.AlertDialogProgress;
import br.com.daciosoftware.roboarm.bluetooth.BluetoothManagerControl;

public class BluetoothFragment extends Fragment implements AdapterView.OnItemClickListener, BluetoothManagerControl.DiscoveryDevices, BluetoothManagerControl.ConnectionDevice {

    private List<BluetoothDevice> listDevices = new ArrayList<>();
    private ListView listViewDevices;
    private BluetoothManagerControl bluetoothManagerControl;
    private AlertDialogProgress alertDialogProgressStartDiscovery;
    private AlertDialogProgress alertDialogProgressPairDevice;
    private Context appContext;
    private Menu menuBluetooth;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context;
        bluetoothManagerControl = BluetoothManagerControl.getInstance(context);
        bluetoothManagerControl.setListenerDiscoveryDevices(BluetoothFragment.this);
        bluetoothManagerControl.setListenerConnectionDevice(BluetoothFragment.this);

        alertDialogProgressStartDiscovery = new AlertDialogProgress(context, AlertDialogProgress.TypeDialog.SEARCH_DEVICE);
        alertDialogProgressPairDevice = new AlertDialogProgress(context, AlertDialogProgress.TypeDialog.PAIR_DEVICE);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true); // Torna o menu action bar visivel;

        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        listViewDevices = root.findViewById(R.id.listViewDevices);
        listViewDevices.setOnItemClickListener(this);

        return root;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_bluetooth, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bluetooth_discovery: {
                bluetoothManagerControl.initDiscovery();
                return true;
            }
            case R.id.action_bluetooth_disconnect: {
                bluetoothManagerControl.disconnect();
                return true;
            }
            default:
                return false;
        }

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menuBluetooth = menu;
        updateMenuBluetooth();
    }

    @SuppressLint({"MissingPermission"})
    private void updateMenuBluetooth() {
        if (menuBluetooth != null) {
            BluetoothDevice devicePaired = bluetoothManagerControl.getDevicePaired();
            MenuItem itemDisconnect = menuBluetooth.findItem(R.id.action_bluetooth_disconnect);
            MenuItem itemDiscovery = menuBluetooth.findItem(R.id.action_bluetooth_discovery);
            if (itemDisconnect != null) {
                itemDisconnect.setVisible(devicePaired != null);
            }
            if (itemDiscovery != null) {
                itemDiscovery.setVisible(devicePaired == null);
            }
            AppCompatActivity activity = (AppCompatActivity) appContext;
            String message = ((devicePaired != null) ? devicePaired.getName() : null);
            activity.getSupportActionBar().setSubtitle(message);
            activity.invalidateOptionsMenu();
        }
    }

    @SuppressLint({"MissingPermission"})
    private void loadDevicesBonded() {
        if (listDevices.size() == 0 ) {
            listDevices = bluetoothManagerControl.getBoundedDevices();
        }
        BluetoothDevicesAdapter devicesBluetoothAdapter = new BluetoothDevicesAdapter(appContext, listDevices);
        listViewDevices.setAdapter(devicesBluetoothAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDevicesBonded();
        //updateMenuBluetooth();
    }

    //Pareamento de dispositivos no click do item da listview
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Pareamento de dispositivos no click do item da listview
        if (bluetoothManagerControl.getDevicePaired() != null) {
            Toast.makeText(appContext, R.string.message_paired_device_already_exists, Toast.LENGTH_SHORT).show();
            return;
        }
        BluetoothDevice newDevicePaired = listDevices.get(position);
        bluetoothManagerControl.connect(newDevicePaired);
    }

    @Override
    public void initDiscoveryDevices() {
        listDevices.clear();
        alertDialogProgressStartDiscovery.show();
    }

    @Override
    public void finishDiscoveryDevices() {
        BluetoothDevicesAdapter adapter = new BluetoothDevicesAdapter(appContext, listDevices);
        listViewDevices.setAdapter(adapter);
        alertDialogProgressStartDiscovery.dismiss();
    }

    @Override
    public void foundDevice(BluetoothDevice device) {
        if (!listDevices.contains(device)) {
            listDevices.add(device);
        }
    }

    @Override
    public void initConnection() {
        alertDialogProgressPairDevice.show();
    }

    @Override
    public void postDeviceConnection() {
        updateMenuBluetooth();
        alertDialogProgressPairDevice.dismiss();
    }

    @Override
    public void postDeviceDisconnection() {
        Toast.makeText(appContext, R.string.message_despair_device, Toast.LENGTH_SHORT).show();
        updateMenuBluetooth();
    }

    @Override
    public void postFailConnection() {
        Toast.makeText(appContext, R.string.message_fail_connection, Toast.LENGTH_SHORT).show();
        alertDialogProgressPairDevice.dismiss();
    }

    @Override
    public void postDataReceived(String dataReceived) {
        AppCompatActivity activity = (AppCompatActivity) appContext;
        String textSubtitle = activity.getSupportActionBar().getSubtitle().toString();
        activity.getSupportActionBar().setSubtitle(textSubtitle + "Bateria: "+ dataReceived);
        activity.invalidateOptionsMenu();
    }
}