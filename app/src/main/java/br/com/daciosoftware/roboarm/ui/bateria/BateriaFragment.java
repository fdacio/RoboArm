package br.com.daciosoftware.roboarm.ui.bateria;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import br.com.daciosoftware.roboarm.MainActivity;
import br.com.daciosoftware.roboarm.R;
import br.com.daciosoftware.roboarm.bluetooth.BluetoothManagerControl;

public class BateriaFragment extends Fragment implements BluetoothManagerControl.ConnectionDevice {
    private static final String SHARED_PREF = "RoboArm";
    private static final String SWITCH_BATTERY = "SwitchBateria";
    private Context appContext;
    private BluetoothManagerControl bluetoothManagerControl;
    private SwitchCompat switchBateria;
    private TextView textViewPercBateria;
    private Toolbar toolbar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context;
        bluetoothManagerControl = BluetoothManagerControl.getInstance(context);
        bluetoothManagerControl.setListenerConnectionDevice(BateriaFragment.this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_bateria, container, false);

        toolbar = root.findViewById(R.id.toolbarBateria);

        switchBateria = root.findViewById(R.id.switchBattery);
        textViewPercBateria = root.findViewById(R.id.textViewPercBateria);
        switchBateria.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String command = (isChecked) ? "BTS\n" : "BTN\n";
            bluetoothManagerControl.write(command.getBytes());
            if (switchBateria.isChecked()) {
                switchBateria.setText(R.string.text_switch_battery);
            } else {
                switchBateria.setText(R.string.text_switch_fonte);
            }
            ((MainActivity)appContext).setSwitchBattery(switchBateria.isChecked());
        });

        updateStatusDevicePaired();

        bluetoothManagerControl.write(String.format("%s\n", "F3").getBytes());
        switchBateria.setChecked(((MainActivity)appContext).getSwitchBattery());

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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        if (dataReceived.contains("bat")) {
            String perc = dataReceived.substring(3);
            textViewPercBateria.setText(String.format(Locale.getDefault(), "%s%%", perc));
        }
    }
}