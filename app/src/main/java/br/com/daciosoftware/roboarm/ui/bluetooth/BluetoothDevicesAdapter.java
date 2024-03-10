package br.com.daciosoftware.roboarm.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.daciosoftware.roboarm.R;

public class BluetoothDevicesAdapter extends BaseAdapter {
    private final LayoutInflater layoutInflater;
    private final List<BluetoothDevice> devices = new ArrayList<>();
    private boolean flagHeaderPair;
    private boolean flagHeaderFound;

    @SuppressLint("MissingPermission")
    public BluetoothDevicesAdapter(Context context, List<BluetoothDevice> devices) {
        layoutInflater = LayoutInflater.from(context);
        for (BluetoothDevice device : devices) {
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                this.devices.add(device);
            }
        }
        for (BluetoothDevice device : devices) {
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                this.devices.add(device);
            }
        }
        flagHeaderPair = true;
        flagHeaderFound = true;

    }

    public int getCount() {
        return devices.size();
    }

    public BluetoothDevice getItem(int position) {
        return devices.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"MissingPermission", "InflateParams"})
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.devices_adapter, null);
            holder = new ViewHolder();
            holder.textViewHeaderPair = convertView.findViewById(R.id.textViewHeaderPair);
            holder.textViewHeaderFound = convertView.findViewById(R.id.textViewHeaderFound);
            holder.deviceName = convertView.findViewById(R.id.textViewDeviceName);
            holder.deviceAddress = convertView.findViewById(R.id.textViewDeviceAddress);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textViewHeaderPair.setVisibility(View.GONE);
        holder.textViewHeaderFound.setVisibility(View.GONE);

        BluetoothDevice device = devices.get(position);

        if ((device.getBondState() == BluetoothDevice.BOND_BONDED) && flagHeaderPair) {
            holder.textViewHeaderPair.setVisibility(View.VISIBLE);
            holder.textViewHeaderFound.setVisibility(View.GONE);
            flagHeaderPair = false;
        }
        if ((device.getBondState() == BluetoothDevice.BOND_NONE) && flagHeaderFound) {
            holder.textViewHeaderPair.setVisibility(View.GONE);
            holder.textViewHeaderFound.setVisibility(View.VISIBLE);
            flagHeaderFound = false;
        }

        String nameDevice = (device.getName() == null) ? "Dispositivo " + position : device.getName();
        holder.deviceName.setText(nameDevice);
        holder.deviceAddress.setText(device.getAddress());

        return convertView;
    }

    static class ViewHolder {
        TextView textViewHeaderPair;
        TextView textViewHeaderFound;
        TextView deviceName;
        TextView deviceAddress;
    }


}
