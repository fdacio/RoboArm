package br.com.daciosoftware.roboarm.ui.bluetotooth;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Set;

import br.com.daciosoftware.roboarm.BluetoothConnectionListener;
import br.com.daciosoftware.roboarm.BluetoothConnectionTask;
import br.com.daciosoftware.roboarm.BluetoothInstance;
import br.com.daciosoftware.roboarm.DevicesBluetoothAdapter;
import br.com.daciosoftware.roboarm.R;

public class BluetoothFragment extends Fragment implements AdapterView.OnItemClickListener, BluetoothConnectionListener {

    private final static int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_PERMISSION_BLUETOOTH = 2;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
    private ListView listViewDevices;
    private BroadcastReceiver receiver;
    private ProgressDialog mProgressDlg;
    private BluetoothDevice devicePaired;
    private Context mContext;
    public Handler mHandler;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true); // Torna o menu action bar visivel;

        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    listDevices.clear();
                    mProgressDlg.show();
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    mProgressDlg.dismiss();
                    DevicesBluetoothAdapter devicesBluetoothAdapter = new DevicesBluetoothAdapter(mContext);
                    devicesBluetoothAdapter.setData(listDevices);
                    listViewDevices.setAdapter(devicesBluetoothAdapter);
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (!listDevices.contains(device)) {
                        Log.i("RoboArm", device.getAddress());
                        listDevices.add(device);
                    }
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    setDisconnected();
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                    setDisconnected();
                }

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        mContext.getApplicationContext().registerReceiver(receiver, filter);

        listViewDevices = root.findViewById(R.id.listViewDevices);
        listViewDevices.setOnItemClickListener(this);

        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        mProgressDlg = new ProgressDialog(mContext);

        mProgressDlg.setTitle("Bluetooth");
        mProgressDlg.setMessage("Aguarde, procurando por dispositivos.");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                bluetoothAdapter.cancelDiscovery();
            }
        });

        enableBluetooth();
        grantAccessLocation();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                AppCompatActivity activity = (AppCompatActivity) mContext;
                String percBateria = message.getData().getString("dados");
                String conectadoCom = devicePaired.getName();
                String textoSubtitle = String.format("Conectado com %s - Bateria: %s%%", conectadoCom, percBateria);
                activity.getSupportActionBar().setSubtitle(textoSubtitle);
                activity.invalidateOptionsMenu();
                super.handleMessage(message);
            }
        };

        return root;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_bluetooth, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bluetooth: {
                bluetoothAdapter.startDiscovery();
                return true;
            }
            case R.id.action_bluetooth_disconnect: {
                BluetoothConnectionTask bluetoothConnection = BluetoothInstance.getInstance();
                if (bluetoothConnection != null) {
                    bluetoothConnection.disconnect();
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem itemDisconnect = menu.findItem(R.id.action_bluetooth_disconnect);
        MenuItem itemDiscovery = menu.findItem(R.id.action_bluetooth);
        BluetoothConnectionTask bluetoothConnection = BluetoothInstance.getInstance();
        if (bluetoothConnection == null) {
            itemDiscovery.setVisible(true);
            itemDisconnect.setVisible(false);
        } else {
            itemDisconnect.setVisible(bluetoothConnection.isConnected());
            itemDiscovery.setVisible(!bluetoothConnection.isConnected());
        }
    }

    private void grantAccessLocation() {
        int permissionCoarseLocation = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionFineLocation = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
        if ((permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) || (permissionFineLocation != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_BLUETOOTH);
        }
    }

    private void enableBluetooth() {
        if (bluetoothAdapter == null) {
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);

            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    //Dispositivo pareados anteriormente
    private void loadDevicesBonded() {
        Set<BluetoothDevice> bondedDevice = bluetoothAdapter.getBondedDevices();
        listDevices.clear();
        for (BluetoothDevice device : bondedDevice) {
            listDevices.add(device);
        }
        DevicesBluetoothAdapter devicesBluetoothAdapter = new DevicesBluetoothAdapter(mContext);
        devicesBluetoothAdapter.setData(listDevices);
        listViewDevices.setAdapter(devicesBluetoothAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_ENABLE_BLUETOOTH) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            String bluetoothDeviceName = bluetoothAdapter.getName();
            String bluetoothDeviceAdrress = bluetoothAdapter.getAddress();
            Toast.makeText(mContext, bluetoothDeviceName + ":" + bluetoothDeviceAdrress, Toast.LENGTH_LONG).show();

            Intent discoverableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivity(discoverableBluetoothIntent);

        }
    }

    @Override
    public void onPause() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        loadDevicesBonded();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mContext.getApplicationContext().unregisterReceiver(receiver);
        super.onDestroy();
    }

    //Pareamento de dispositivos no click do item da listview
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (BluetoothInstance.isConnected()) {
            Toast.makeText(mContext, "Dispositivo já conectado", Toast.LENGTH_LONG).show();
            return;
        }
        devicePaired = listDevices.get(position);
        BluetoothConnectionTask bluetoothConnection = new BluetoothConnectionTask(devicePaired, this, mContext);
        BluetoothInstance.setInstance(bluetoothConnection);
        if (bluetoothConnection.getStatus() == AsyncTask.Status.PENDING || bluetoothConnection.getStatus() == AsyncTask.Status.FINISHED) {
            bluetoothConnection.execute();
        }
    }

    @Override
    public void setConnected(BluetoothDevice device) {
        AppCompatActivity activity = (AppCompatActivity) mContext;
        String message = "Conectado com " + device.getName();
        activity.getSupportActionBar().setSubtitle(message);
        activity.invalidateOptionsMenu();
    }

    @Override
    public void setDisconnected() {
        AppCompatActivity activity = (AppCompatActivity) mContext;
        activity.getSupportActionBar().setSubtitle(null);
        activity.invalidateOptionsMenu();
    }

    @Override
    public void readFromDevicePaired(String dadosLido) {
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("dados", dadosLido);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

}