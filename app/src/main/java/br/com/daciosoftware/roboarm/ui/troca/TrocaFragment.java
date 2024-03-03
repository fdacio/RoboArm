package br.com.daciosoftware.roboarm.ui.troca;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import br.com.daciosoftware.roboarm.R;
import br.com.daciosoftware.roboarm.bluetooth.BluetoothManagerControl;
import br.com.daciosoftware.roboarm.ui.bluetooth.BluetoothFragment;

public class TrocaFragment extends Fragment implements BluetoothManagerControl.ConnectionDevice {
    private Context appContext;
    private BluetoothManagerControl bluetoothManagerControl;
    private Toolbar toolbar;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context;
        bluetoothManagerControl = BluetoothManagerControl.getInstance(context);
        bluetoothManagerControl.setListenerConnectionDevice(TrocaFragment.this);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_troca, container, false);

        toolbar = root.findViewById(R.id.toolbarTroca);

        Button buttonStart = root.findViewById(R.id.button_start);
        Button buttonStop = root.findViewById(R.id.button_stop);
        Button buttonSpeed1 = root.findViewById(R.id.button_speed1);
        Button buttonSpeed2 = root.findViewById(R.id.button_speed2);

        buttonStart.setOnClickListener(v -> {
            BluetoothDevice devicePaired = bluetoothManagerControl.getDevicePaired();
            if (devicePaired  == null) {
                Toast.makeText(appContext, R.string.message_dont_device_pair, Toast.LENGTH_LONG).show();
                return;
            }
            String command = "A\n";
            bluetoothManagerControl.write(command.getBytes());
        });

        buttonStop.setOnClickListener(v -> {
            BluetoothDevice devicePaired = bluetoothManagerControl.getDevicePaired();
            if (devicePaired  == null) {
                Toast.makeText(appContext, R.string.message_dont_device_pair, Toast.LENGTH_LONG).show();
                return;
            }
            String command = "B\n";
            bluetoothManagerControl.write(command.getBytes());
        });

        buttonSpeed1.setOnClickListener(v -> {
            BluetoothDevice devicePaired = bluetoothManagerControl.getDevicePaired();
            if (devicePaired  == null) {
                Toast.makeText(appContext, R.string.message_dont_device_pair, Toast.LENGTH_LONG).show();
                return;
            }
            String command = "C\n";
            bluetoothManagerControl.write(command.getBytes());
        });

        buttonSpeed2.setOnClickListener(v -> {
            BluetoothDevice devicePaired = bluetoothManagerControl.getDevicePaired();
            if (devicePaired  == null) {
                Toast.makeText(appContext, R.string.message_dont_device_pair, Toast.LENGTH_LONG).show();
                return;
            }
            String command = "D\n";
            bluetoothManagerControl.write(command.getBytes());
        });
        updateStatusDevicePaired();
        return root;
    }
    @SuppressLint({"MissingPermission"})
    private void updateStatusDevicePaired() {
        BluetoothDevice devicePaired = bluetoothManagerControl.getDevicePaired();
        toolbar.setSubtitle((devicePaired != null) ? devicePaired.getName() : null);
    }
    @Override
    public void onResume() {
        super.onResume();
        bluetoothManagerControl.write(String.format("%s\n", "F1").getBytes());
    }

    @Override
    public void initConnection(BluetoothDevice device) {

    }

    @Override
    public void postDeviceConnection(BluetoothDevice device) {

    }

    @Override
    public void postDeviceDisconnection() {
        Toast.makeText(appContext, R.string.message_despair_device, Toast.LENGTH_SHORT).show();
        updateStatusDevicePaired();
    }

    @Override
    public void postFailConnection(BluetoothDevice device) {

    }

    @Override
    public void postDataReceived(String dataReceived) {

    }
}