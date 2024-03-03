package br.com.daciosoftware.roboarm.alertdialog;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import br.com.daciosoftware.roboarm.R;

public class AlertDialogInformation {
    private final AlertDialog dialog;

    public AlertDialogInformation(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNeutralButton(R.string.dialog_neutral_button, (d, w) -> d.dismiss());
        dialog = builder.create();

    }

    public void show() {
        dialog.show();
    }

}
