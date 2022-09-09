package br.com.daciosoftware.roboarm.ui.comandos;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import br.com.daciosoftware.roboarm.BluetoothConnection;
import br.com.daciosoftware.roboarm.BluetoothInstance;
import br.com.daciosoftware.roboarm.R;

public class ComandosFragment extends Fragment {

    private View root;
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_comandos, container, false);

        Button buttonStart = root.findViewById(R.id.button_start);
        Button buttonStop = root.findViewById(R.id.button_stop);
        Button buttonSpeed1 = root.findViewById(R.id.button_speed1);
        Button buttonSpeed2 = root.findViewById(R.id.button_speed2);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BluetoothInstance.isConnected()) {
                    Toast.makeText(mContext, "Não há dispositivo conectado", Toast.LENGTH_LONG).show();
                    return;
                }
                BluetoothConnection bluetoothConnection = BluetoothInstance.getInstance();
                String command = "A\n";
                bluetoothConnection.write(command.getBytes());
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BluetoothInstance.isConnected()) {
                    Toast.makeText(mContext, "Não há dispositivo conectado", Toast.LENGTH_LONG).show();
                    return;
                }
                BluetoothConnection bluetoothConnection = BluetoothInstance.getInstance();
                String command = "B\n";
                bluetoothConnection.write(command.getBytes());
            }
        });

        buttonSpeed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BluetoothInstance.isConnected()) {
                    Toast.makeText(mContext, "Não há dispositivo conectado", Toast.LENGTH_LONG).show();
                    return;
                }
                BluetoothConnection bluetoothConnection = BluetoothInstance.getInstance();
                String command = "C\n";
                bluetoothConnection.write(command.getBytes());
            }
        });

        buttonSpeed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BluetoothInstance.isConnected()) {
                    Toast.makeText(mContext, "Não há dispositivo conectado", Toast.LENGTH_LONG).show();
                    return;
                }
                BluetoothConnection bluetoothConnection = BluetoothInstance.getInstance();
                String command = "D\n";
                bluetoothConnection.write(command.getBytes());
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        BluetoothConnection bluetoothConnection = BluetoothInstance.getInstance();
        if (BluetoothInstance.isConnected()) {
            bluetoothConnection.write("F1\n".getBytes());
        }
    }
}