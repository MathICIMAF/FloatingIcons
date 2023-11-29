package com.amg.appsflotantes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class MyDialogFragment extends DialogFragment {
    public static String SText = "TEXT";
    public static String STitle = "TITLE";
    String text = "";
    String title = "";

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.title = getArguments().getString(STitle, "");
        this.text = getArguments().getString(SText, "");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Permitir interferir visualmente");
        builder.setMessage("Para que la App funcione es necesario que habilite el permiso de interferir visualmente sobre otras aplicaciones");
        builder.setPositiveButton("HABILITAR", new DialogInterface.OnClickListener() {
            /* class com.amg.appsflotantes.MyDialogFragment.AnonymousClass1 */

            public void onClick(DialogInterface dialog, int id) {
                MyDialogFragment.this.startActivityForResult(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:com.amg.appsflotantes")), 0);
                MyDialogFragment.this.dismiss();
            }
        });
        return builder.create();
    }
}
