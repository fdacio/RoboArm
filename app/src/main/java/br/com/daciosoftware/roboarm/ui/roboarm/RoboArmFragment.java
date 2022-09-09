package br.com.daciosoftware.roboarm.ui.roboarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import br.com.daciosoftware.roboarm.BluetoothConnection;
import br.com.daciosoftware.roboarm.BluetoothInstance;
import br.com.daciosoftware.roboarm.R;

public class RoboArmFragment extends Fragment {

    private View root;
    private Context mContext;
    private SeekBar seekBarServoBase;
    private SeekBar seekBarServoAltura;
    private SeekBar seekBarServoAngulo;
    private SeekBar seekBarServoGarra;

    private TextView textViewValorBase;
    private TextView textViewValorAltura;
    private TextView textViewValorAngulo;
    private TextView textViewValorGarra;

    public static final String SHARED_PREF = "RoboArm";
    public static final String BASE= "base";
    public static final String ALTURA = "altura";
    public static final String ANGULO = "angulo";
    public static final String GARRA = "garra";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_roboarm, container, false);

        seekBarServoBase = root.findViewById(R.id.seekBarServoBase);
        seekBarServoAltura = root.findViewById(R.id.seekBarServoAltura);
        seekBarServoAngulo = root.findViewById(R.id.seekBarServoAngulo);
        seekBarServoGarra = root.findViewById(R.id.seekBarServoGarra);

        textViewValorBase = root.findViewById(R.id.textViewValorBase);
        textViewValorAltura = root.findViewById(R.id.textViewValorAltura);
        textViewValorAngulo = root.findViewById(R.id.textViewValorAngulo);
        textViewValorGarra = root.findViewById(R.id.textViewValorGarra);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREF, 0);
        seekBarServoBase.setProgress(sharedPreferences.getInt(BASE, 0));
        seekBarServoAltura.setProgress(sharedPreferences.getInt(ALTURA, 0));
        seekBarServoAngulo.setProgress(sharedPreferences.getInt(ANGULO, 0));
        seekBarServoGarra.setProgress(sharedPreferences.getInt(GARRA, 0));

        seekBarServoBase.setOnSeekBarChangeListener(new SeekBarChange(textViewValorBase));
        seekBarServoAltura.setOnSeekBarChangeListener(new SeekBarChange(textViewValorAltura));
        seekBarServoAngulo.setOnSeekBarChangeListener(new SeekBarChange(textViewValorAngulo));
        seekBarServoGarra.setOnSeekBarChangeListener(new SeekBarChange(textViewValorGarra));

        textViewValorBase.setText(String.valueOf(seekBarServoBase.getProgress())+"°");
        textViewValorAltura.setText(String.valueOf(seekBarServoAltura.getProgress())+"°");
        textViewValorAngulo.setText(String.valueOf(seekBarServoAngulo.getProgress())+"°");
        textViewValorGarra.setText(String.valueOf(seekBarServoGarra.getProgress())+"°");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        int servoBase = seekBarServoBase.getProgress();
        int servoAltura= seekBarServoAltura.getProgress();
        int servoAngulo = seekBarServoAngulo.getProgress();
        int servoGarra = seekBarServoGarra.getProgress();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(BASE, servoBase);
        editor.putInt(ALTURA, servoAltura);
        editor.putInt(ANGULO, servoAngulo);
        editor.putInt(GARRA, servoGarra);
        editor.apply();
    }

    private class SeekBarChange implements SeekBar.OnSeekBarChangeListener {

        private TextView text;

        public SeekBarChange(TextView text) {
            this.text = text;
        }
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            this.text.setText(String.format("%dº", progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            sendData(seekBar);
        }

        private void sendData(SeekBar seekBar) {
            String command = "";
            if (seekBar.getId() == R.id.seekBarServoBase) {
                command = String.format("bs%d\n", seekBar.getProgress());
            }
            if (seekBar.getId() == R.id.seekBarServoAltura) {
                command = String.format("at%d\n", seekBar.getProgress());
            }
            if (seekBar.getId() == R.id.seekBarServoAngulo) {
                command = String.format("ag%d\n", seekBar.getProgress());
            }
            if (seekBar.getId() == R.id.seekBarServoGarra) {
                command = String.format("gr%d\n", seekBar.getProgress());
            }
            if (!BluetoothInstance.isConnected()) {
                Toast.makeText(mContext, "Não há dispositivo conectado", Toast.LENGTH_LONG).show();
                return;
            }
            BluetoothConnection bluetoothConnection = BluetoothInstance.getInstance();
            bluetoothConnection.write(command.getBytes());
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        BluetoothConnection bluetoothConnection = BluetoothInstance.getInstance();
        if (BluetoothInstance.isConnected()) {
            bluetoothConnection.write("F2\n".getBytes());
        }
    }
}