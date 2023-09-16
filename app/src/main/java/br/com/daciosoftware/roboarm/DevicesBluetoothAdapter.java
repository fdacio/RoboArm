package br.com.daciosoftware.roboarm;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class DevicesBluetoothAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<BluetoothDevice> mData;

    public DevicesBluetoothAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<BluetoothDevice> data) {
        mData = data;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("MissingPermission")
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.devices_adapter, null);

            holder = new ViewHolder();

            holder.nameTv = (TextView) convertView.findViewById(R.id.textViewDeviceName);
            holder.addressTv = (TextView) convertView.findViewById(R.id.textViewDeviceAddress);
            //holder.pairBtn		= (Button) convertView.findViewById(R.id.btn_pair);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = mData.get(position);
        String nameDevice = (device.getName() == null) ? "Dispositivo " + position : device.getName();
        holder.nameTv.setText(nameDevice);
        holder.addressTv.setText(device.getAddress());

        return convertView;
    }

    static class ViewHolder {
        TextView nameTv;
        TextView addressTv;
    }


}
