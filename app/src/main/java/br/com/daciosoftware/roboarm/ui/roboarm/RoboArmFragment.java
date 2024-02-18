package br.com.daciosoftware.roboarm.ui.roboarm;

import android.bluetooth.BluetoothDevice;
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

import java.util.Locale;

import br.com.daciosoftware.roboarm.R;
import br.com.daciosoftware.roboarm.bluetooth.BluetoothManagerControl;
import br.com.daciosoftware.roboarm.ui.bateria.BateriaFragment;

public class RoboArmFragment extends Fragment implements BluetoothManagerControl.ConnectionDataReceive {

    private Context appContext;
    private SeekBar seekBarServoBase;
    private SeekBar seekBarServoAltura;
    private SeekBar seekBarServoAngulo;
    private SeekBar seekBarServoGarra;

    public static final String SHARED_PREF = "RoboArm";
    public static final String BASE = "base";
    public static final String ALTURA = "altura";
    public static final String ANGULO = "angulo";
    public static final String GARRA = "garra";

    private BluetoothManagerControl bluetoothManagerControl;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context;
        bluetoothManagerControl = BluetoothManagerControl.getInstance(context);
        bluetoothManagerControl.setListenerConnectionDataReceive(RoboArmFragment.this);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_roboarm, container, false);

        seekBarServoBase = root.findViewById(R.id.seekBarServoBase);
        seekBarServoAltura = root.findViewById(R.id.seekBarServoAltura);
        seekBarServoAngulo = root.findViewById(R.id.seekBarServoAngulo);
        seekBarServoGarra = root.findViewById(R.id.seekBarServoGarra);

        TextView textViewValorBase = root.findViewById(R.id.textViewValorBase);
        TextView textViewValorAltura = root.findViewById(R.id.textViewValorAltura);
        TextView textViewValorAngulo = root.findViewById(R.id.textViewValorAngulo);
        TextView textViewValorGarra = root.findViewById(R.id.textViewValorGarra);

/*        SharedPreferences sharedPreferences = appContext.getSharedPreferences(SHARED_PREF, 0);
        seekBarServoBase.setProgress(sharedPreferences.getInt(BASE, 0));
        seekBarServoAltura.setProgress(sharedPreferences.getInt(ALTURA, 180));
        seekBarServoAngulo.setProgress(sharedPreferences.getInt(ANGULO, 70));
        seekBarServoGarra.setProgress(sharedPreferences.getInt(GARRA, 180));*/

        seekBarServoBase.setOnSeekBarChangeListener(new SeekBarChange(textViewValorBase));
        seekBarServoAltura.setOnSeekBarChangeListener(new SeekBarChange(textViewValorAltura));
        seekBarServoAngulo.setOnSeekBarChangeListener(new SeekBarChange(textViewValorAngulo));
        seekBarServoGarra.setOnSeekBarChangeListener(new SeekBarChange(textViewValorGarra));

        textViewValorBase.setText(String.format(Locale.getDefault(), "%d°", seekBarServoBase.getProgress()));
        textViewValorAltura.setText(String.format(Locale.getDefault(), "%d°", seekBarServoAltura.getProgress()));
        textViewValorAngulo.setText(String.format(Locale.getDefault(), "%d°", seekBarServoAngulo.getProgress()));
        textViewValorGarra.setText(String.format(Locale.getDefault(), "%d°", seekBarServoGarra.getProgress()));

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
/*        int servoBase = seekBarServoBase.getProgress();
        int servoAltura= seekBarServoAltura.getProgress();
        int servoAngulo = seekBarServoAngulo.getProgress();
        int servoGarra = seekBarServoGarra.getProgress();
        SharedPreferences sharedPreferences = appContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(BASE, servoBase);
        editor.putInt(ALTURA, servoAltura);
        editor.putInt(ANGULO, servoAngulo);
        editor.putInt(GARRA, servoGarra);
        editor.apply();*/
    }

    @Override
    public void onResume() {
        super.onResume();
        bluetoothManagerControl.write(String.format("%s\n", "F2").getBytes());
    }

    @Override
    public void postDataReceived(String dataReceived) {
        if (dataReceived.contains("ANGULOS")) {

            String[] angulos = dataReceived.split("-");
            String anguloBase = angulos[0];
            String anguloAltura = angulos[1];
            String anguloAngulo = angulos[2];
            String anguloGarra = angulos[3];

            String[] dataAnguloBase = anguloBase.split(";");
            String[] dataAnguloAltura = anguloAltura.split(";");
            String[] dataAnguloAngulo = anguloAngulo.split(";");
            String[] dataAnguloGarra = anguloGarra.split(";");

            Integer valorAnguloBase = Integer.valueOf(dataAnguloBase[1]);
            Integer valorAnguloAltura = Integer.valueOf(dataAnguloAltura[1]);
            Integer valorAnguloAngulo = Integer.valueOf(dataAnguloAngulo[1]);
            Integer valorAnguloGarra = Integer.valueOf(dataAnguloGarra[1]);

            seekBarServoBase.setProgress(valorAnguloBase);
            seekBarServoAltura.setProgress(valorAnguloAltura);
            seekBarServoAngulo.setProgress(valorAnguloAngulo);
            seekBarServoGarra.setProgress(valorAnguloGarra);
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
                command = String.format(Locale.getDefault(), "bs%d\n", seekBar.getProgress());
            }
            if (seekBar.getId() == R.id.seekBarServoAltura) {
                command = String.format(Locale.getDefault(), "at%d\n", seekBar.getProgress());
            }
            if (seekBar.getId() == R.id.seekBarServoAngulo) {
                command = String.format(Locale.getDefault(), "ag%d\n", seekBar.getProgress());
            }
            if (seekBar.getId() == R.id.seekBarServoGarra) {
                command = String.format(Locale.getDefault(), "gr%d\n", seekBar.getProgress());
            }
            BluetoothDevice devicePaired = bluetoothManagerControl.getDevicePaired();

            if (devicePaired == null) {
                Toast.makeText(appContext, "Não há dispositivo conectado", Toast.LENGTH_LONG).show();
                return;
            }
            bluetoothManagerControl.write(command.getBytes());
        }
    }
}