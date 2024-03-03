package br.com.daciosoftware.roboarm.alertdialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import br.com.daciosoftware.roboarm.R;

public class AlertDialogDevicePaired {
    public enum TypeDialog {
        SUCCESS_PAIR, FAIL_PAIR
    }
    private final AlertDialog dialog;
    private final TextView textViewDeviceName;

    public AlertDialogDevicePaired(Context context, TypeDialog typeDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View convertView = (typeDialog == TypeDialog.SUCCESS_PAIR) ? inflater.inflate(R.layout.alert_dialog_device_success_pair,null) : inflater.inflate(R.layout.alert_dialog_device_fail_pair,null);
        textViewDeviceName = convertView.findViewById(R.id.textViewDeviceNamePairing);
        builder.setView(convertView);
        builder.setPositiveButton(R.string.dialog_neutral_button, (d, w) -> d.dismiss());
        dialog = builder.create();
    }

    public void show(String deviceName) {
        textViewDeviceName.setText(deviceName);
        dialog.show();
    }

}
