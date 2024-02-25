package br.com.daciosoftware.roboarm.ui.bateria;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import br.com.daciosoftware.roboarm.R;
import br.com.daciosoftware.roboarm.bluetooth.BluetoothManagerControl;

public class BateriaFragment extends Fragment implements BluetoothManagerControl.ConnectionDataReceive {
    private static final String SHARED_PREF = "RoboArm";
    private static final String SWITCH_BATTERY = "SwitchBateria";
    private Context appContext;
    private BluetoothManagerControl bluetoothManagerControl;
    private SwitchCompat switchCompat;
    private TextView textViewPercBateria;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context;
        bluetoothManagerControl = BluetoothManagerControl.getInstance(context);
        bluetoothManagerControl.setListenerConnectionDataReceive(BateriaFragment.this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_bateria, container, false);

        switchCompat = root.findViewById(R.id.switchBattery);
        textViewPercBateria = root.findViewById(R.id.textViewPercBateria);
        textViewPercBateria.setVisibility(View.INVISIBLE);
        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String command = (isChecked) ? "BTS\n" : "BTN\n";
            textViewPercBateria.setVisibility((isChecked) ? View.VISIBLE : View.INVISIBLE);
            bluetoothManagerControl.write(command.getBytes());
        });

        SharedPreferences sharedPreferences = appContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        int switchBattery = sharedPreferences.getInt(SWITCH_BATTERY, 0);
        switchCompat.setChecked((switchBattery) == 0 ? false : true);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SharedPreferences sharedPreferences = appContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SWITCH_BATTERY, (switchCompat.isChecked()) ? 1 : 0);
        editor.apply();
    }

    @Override
    public void postDataReceived(String dataReceived) {
        if (dataReceived.contains("BAT")) {
            String[] data = dataReceived.split(";");
            textViewPercBateria.setText(String.format(Locale.getDefault(), "%s%%", data[1]));
        }
    }
}