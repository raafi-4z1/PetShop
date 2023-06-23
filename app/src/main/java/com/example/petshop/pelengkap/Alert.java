package com.example.petshop.pelengkap;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.petshop.R;

import org.json.JSONException;

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
}