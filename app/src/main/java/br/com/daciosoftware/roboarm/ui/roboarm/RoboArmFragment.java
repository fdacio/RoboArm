package br.com.daciosoftware.roboarm.ui.roboarm;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import br.com.daciosoftware.roboarm.R;
import br.com.daciosoftware.roboarm.bluetooth.BluetoothManagerControl;
import br.com.daciosoftware.roboarm.ui.bateria.BateriaDTO;

public class RoboArmFragment extends Fragment implements BluetoothManagerControl.ConnectionDevice {

    private Context appContext;
    private SeekBar seekBarServoBase;
    private SeekBar seekBarServoAltura;
    private SeekBar seekBarServoAlcance;
    private SeekBar seekBarServoGarra;
    private Toolbar toolbar;
    private TextView textViewPercBattery;
    private SwitchCompat switchSendData;
    private BluetoothManagerControl bluetoothManagerControl;
    private BateriaDTO bateriaDTO;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context;
        bluetoothManagerControl = BluetoothManagerControl.getInstance(context);
        bluetoothManagerControl.setListenerConnectionDevice(RoboArmFragment.this);
        bateriaDTO = BateriaDTO.getInstance();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_roboarm, container, false);

        toolbar = root.findViewById(R.id.toolbarRoboarm);

        seekBarServoBase = root.findViewById(R.id.seekBarServoBase);
        seekBarServoAltura = root.findViewById(R.id.seekBarServoAltura);
        seekBarServoAlcance = root.findViewById(R.id.seekBarServoAlcance);
        seekBarServoGarra = root.findViewById(R.id.seekBarServoGarra);

        TextView textViewValorBase = root.findViewById(R.id.textViewValorAnguloBase);
        TextView textViewValorAltura = root.findViewById(R.id.textViewValorAnguloAltura);
        TextView textViewValorAlcance = root.findViewById(R.id.textViewValorAnguloAlcance);
        TextView textViewValorGarra = root.findViewById(R.id.textViewValorAnguloGarra);

        switchSendData = root.findViewById(R.id.switchSendData);
        switchSendData.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String command = (isChecked) ? "sp0\n" : "sp1\n";
            bluetoothManagerControl.write(command.getBytes());
        });

        textViewPercBattery = root.findViewById(R.id.textViewPercBatteryToolbar);
        textViewPercBattery.setText(String.format(Locale.getDefault(), "%s%%", bateriaDTO.getPerc()));

        seekBarServoBase.setOnSeekBarChangeListener(new SeekBarChange(textViewValorBase));
        seekBarServoAltura.setOnSeekBarChangeListener(new SeekBarChange(textViewValorAltura));
        seekBarServoAlcance.setOnSeekBarChangeListener(new SeekBarChange(textViewValorAlcance));
        seekBarServoGarra.setOnSeekBarChangeListener(new SeekBarChange(textViewValorGarra));

        textViewValorBase.setText(String.format(Locale.getDefault(), "%d°", seekBarServoBase.getProgress()));
        textViewValorAltura.setText(String.format(Locale.getDefault(), "%d°", seekBarServoAltura.getProgress()));
        textViewValorAlcance.setText(String.format(Locale.getDefault(), "%d°", seekBarServoAlcance.getProgress()));
        textViewValorGarra.setText(String.format(Locale.getDefault(), "%d°", seekBarServoGarra.getProgress()));

        updateStatusDevicePaired();

        bluetoothManagerControl.write(String.format("%s\n", "F2").getBytes());

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
    public void postDataReceived(@NonNull String dataReceived) {

        if (dataReceived.contains("ags")) {

            String[] aux = dataReceived.split(":");
            String[] arrayAng = aux[1].split("-");
            String angBase = arrayAng[0];
            String andAltura = arrayAng[1];
            String angAlcance = arrayAng[2];
            String angGarra = arrayAng[3];

            int valorAnguloBase = Integer.parseInt(angBase.substring(2));
            int valorAnguloAltura = Integer.parseInt(andAltura.substring(2));
            int valorAnguloAlcance = Integer.parseInt(angAlcance.substring(2));
            int valorAnguloGarra = Integer.parseInt(angGarra.substring(2));

            seekBarServoBase.setProgress(valorAnguloBase);
            seekBarServoAltura.setProgress(valorAnguloAltura);
            seekBarServoAlcance.setProgress(valorAnguloAlcance);
            seekBarServoGarra.setProgress(valorAnguloGarra);
        }

        if (dataReceived.contains("bat:")) {
            String percAndVolt = dataReceived.substring(4);
            String[] arrayPercAndVolt = percAndVolt.split(";");
            String perc = arrayPercAndVolt[0];
            String volt = arrayPercAndVolt[1];
            bateriaDTO.setPerc(perc);
            bateriaDTO.setVolt(volt);
            textViewPercBattery.setText(String.format(Locale.getDefault(), "%s%%", bateriaDTO.getPerc()));
        }
    }

    private class SeekBarChange implements SeekBar.OnSeekBarChangeListener {
        private final TextView text;
        public SeekBarChange(TextView text) {
            this.text = text;
        }
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            this.text.setText(String.format(Locale.getDefault(), "%dº", progress));
            if(switchSendData.isChecked()) {
                sendData(seekBar);
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(!switchSendData.isChecked()) {
                sendData(seekBar);
            }
        }
        private void sendData(@NonNull SeekBar seekBar) {
            BluetoothDevice devicePaired = bluetoothManagerControl.getDevicePaired();
            if (devicePaired == null) {
                Toast.makeText(appContext, R.string.message_dont_device_pair, Toast.LENGTH_LONG).show();
                return;
            }
            if (seekBar.getId() == R.id.seekBarServoBase) {
                String command = String.format(Locale.getDefault(), "bs%d\n", seekBar.getProgress());
                bluetoothManagerControl.write(command.getBytes());
                return;
            }
            if (seekBar.getId() == R.id.seekBarServoAltura) {
                String command = String.format(Locale.getDefault(), "at%d\n", seekBar.getProgress());
                bluetoothManagerControl.write(command.getBytes());
                return;
            }
            if (seekBar.getId() == R.id.seekBarServoAlcance) {
                String command = String.format(Locale.getDefault(), "ac%d\n", seekBar.getProgress());
                bluetoothManagerControl.write(command.getBytes());
                return;
            }
            if (seekBar.getId() == R.id.seekBarServoGarra) {
                String command = String.format(Locale.getDefault(), "gr%d\n", seekBar.getProgress());
                bluetoothManagerControl.write(command.getBytes());
            }

        }
    }
}