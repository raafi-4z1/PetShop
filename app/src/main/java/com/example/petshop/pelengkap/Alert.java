package com.example.petshop.pelengkap;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.petshop.R;

public class Alert {
    public static void alertFail(String s, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Failed")
                .setIcon(R.drawable.baseline_warning_24)
                .setMessage(s)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Handle OK button click if needed
                })
                .show();
    }

    public static void kode401 (String s, Context context) {
        Toast.makeText(context, s + ". Silahkan Login Kembali!", Toast.LENGTH_LONG).show();
    }

    public static android.app.AlertDialog loading (Context context) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        return builder.create();
    }
}