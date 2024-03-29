package br.com.daciosoftware.roboarm.ui.bateria;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
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

import br.com.daciosoftware.roboarm.R;
import br.com.daciosoftware.roboarm.bluetooth.BluetoothManagerControl;

public class BateriaFragment extends Fragment implements BluetoothManagerControl.ConnectionDevice {
    private Context appContext;
    private BluetoothManagerControl bluetoothManagerControl;
    private SwitchCompat switchBattery;
    private TextView textViewPercBattery;
    private TextView textViewVoltBattery;
    private Toolbar toolbar;

    private BateriaDTO bateriaDTO;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context;
        bluetoothManagerControl = BluetoothManagerControl.getInstance(context);
        bluetoothManagerControl.setListenerConnectionDevice(BateriaFragment.this);
        bateriaDTO = BateriaDTO.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_battery, container, false);

        toolbar = root.findViewById(R.id.toolbarBattery);

        textViewPercBattery = root.findViewById(R.id.textViewPercBattery);
        textViewVoltBattery = root.findViewById(R.id.textViewVoltBattery);

        textViewPercBattery.setText(String.format(Locale.getDefault(), "%s%%", bateriaDTO.getPerc()));
        textViewVoltBattery.setText(String.format(Locale.getDefault(), "%sV", bateriaDTO.getVolt()));

        switchBattery = root.findViewById(R.id.switchBattery);
        switchBattery.setOnClickListener((buttonView) -> {
            BluetoothDevice devicePaired = bluetoothManagerControl.getDevicePaired();
            if (devicePaired  == null) {
                Toast.makeText(appContext, R.string.message_dont_device_pair, Toast.LENGTH_LONG).show();
                return;
            }
            String command = (switchBattery.isChecked()) ? "bts\n" : "btn\n";
            bluetoothManagerControl.write(command.getBytes());
            if (switchBattery.isChecked()) {
                switchBattery.setText(R.string.text_switch_battery);
            } else {
                switchBattery.setText(R.string.text_switch_power_supply);
            }
        });

        updateStatusDevicePaired();

        bluetoothManagerControl.write(String.format("%s\n", "F3").getBytes());

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
        textViewPercBattery.setText(String.format(Locale.getDefault(), "%d%%", 0));
        textViewVoltBattery.setText(String.format(Locale.getDefault(), "%dV", 0));
    }

    @Override
    public void postFailConnection(BluetoothDevice device) {

    }

    @Override
    public void postDataReceived(String dataReceived) {
        if (dataReceived.contains("bt")) {
            switchBattery.setChecked(dataReceived.equals("bts"));
        }
        if (dataReceived.contains("bat:")) {
            String percAndVolt = dataReceived.substring(4);
            String[] arrayPercAndVolt = percAndVolt.split(";");
            String perc = arrayPercAndVolt[0];
            String volt = arrayPercAndVolt[1];
            bateriaDTO.setPerc(perc);
            bateriaDTO.setVolt(volt);
            textViewPercBattery.setText(String.format(Locale.getDefault(), "%s%%", bateriaDTO.getPerc()));
            textViewVoltBattery.setText(String.format(Locale.getDefault(), "%sV", bateriaDTO.getVolt()));
        }
    }
}