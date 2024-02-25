package br.com.daciosoftware.roboarm.ui.troca;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import br.com.daciosoftware.roboarm.R;
import br.com.daciosoftware.roboarm.bluetooth.BluetoothManagerControl;

public class TrocaFragment extends Fragment {
    private Context appContext;
    private BluetoothManagerControl bluetoothManagerControl;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context;
        bluetoothManagerControl = BluetoothManagerControl.getInstance(context);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_troca, container, false);

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

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        bluetoothManagerControl.write(String.format("%s\n", "F1").getBytes());
    }
}