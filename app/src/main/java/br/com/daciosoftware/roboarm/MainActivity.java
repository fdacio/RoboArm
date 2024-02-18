package br.com.daciosoftware.roboarm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import br.com.daciosoftware.roboarm.bluetooth.BluetoothManagerControl;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_BLUETOOTH = 2;
    private final static int REQUEST_ENABLE_BLUETOOTH = 1;
    private BluetoothManagerControl bluetoothManagerControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_bluetooth, R.id.navigation_troca, R.id.navigation_roboarm, R.id.navigation_bateria)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        bluetoothManagerControl = BluetoothManagerControl.getInstance(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0].equals(Manifest.permission.BLUETOOTH_CONNECT)) {
                    bluetoothManagerControl.requestEnableBluetoothAdapter();
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.message_permission_important, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BLUETOOTH) {
            Toast.makeText(MainActivity.this, R.string.message_bluetooth_activate_success, Toast.LENGTH_SHORT).show();
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(MainActivity.this, R.string.message_bluetooth_activate_important, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bluetoothManagerControl.registerBluetoothBroadcastReceive();
    }

    @Override
    public void onStop() {
        super.onStop();
        bluetoothManagerControl.unregisterBluetoothBroadcastReceive();
    }
}