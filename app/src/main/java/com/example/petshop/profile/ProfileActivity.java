package com.example.petshop.profile;

import static com.example.petshop.pelengkap.Alert.loading;
import static com.example.petshop.pelengkap.StringPhone.formatPhone;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petshop.LoginSignup.LoginActivity;
import com.example.petshop.R;
import com.example.petshop.pelengkap.Http;
import com.example.petshop.pelengkap.LocalStorage;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {
    private LocalStorage localStorage;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);

        AlertDialog dialog = loading(ProfileActivity.this);
        dialog.show();

        localStorage = new LocalStorage(this);
        bindViews();

        findViewById(R.id.btnChangePassword).setOnClickListener(view
                -> startActivity(new Intent(this, UpdatePasswordActivity.class)));

        findViewById(R.id.btnEditProfile).setOnClickListener(view
                -> startActivity(new Intent(this, UpdateProfileActivity.class)));

        findViewById(R.id.btnLogoutProfile).setOnClickListener(view
                -> new AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda ingin keluar dari aplikasi?")
                .setPositiveButton("OK", (dialogInterface, which)
                        -> logout())
                .setNegativeButton("Tidak", (dialogInterface, i) -> {})
                .show());

        getJmlHistory(dialog);
    }

    @SuppressLint("SetTextI18n")
    private void bindViews() {
        TextView nama = findViewById(R.id.txtNamaProfile);
        TextView email = findViewById(R.id.txtEmailProfile);
        TextView phone = findViewById(R.id.txtPhoneProfile);
        TextView alamat = findViewById(R.id.txtAlamatProfile);

        nama.setText(localStorage.getNama());
        email.setText(localStorage.getEmail().isEmpty()? "email" : localStorage.getEmail());
        phone.setText("+62 " + formatPhone(localStorage.getTelepon()));
        alamat.setText(localStorage.getAlamat().isEmpty()? "alamat" : localStorage.getAlamat());
    }

    private void getJmlHistory(AlertDialog dialog) {
        String url = getString(R.string.api_server) + "/user/profile";

        Thread thread = new Thread(() -> {
            Http http = new Http(this, url);
            http.setMethod("post");
            http.setToken(true);
            http.send();

            runOnUiThread(() -> {
                JSONObject response = null;
                try {
                    response = new JSONObject(http.getResponse());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int code = http.getStatusCode();
                assert response != null;
                if (code == 200) {
                    try {
                        JSONObject data = response.getJSONObject("data");

                        TextView penitipan = findViewById(R.id.jmlPenitipan);
                        TextView pemesanan = findViewById(R.id.jmlPemesanan);
                        TextView transaksi = findViewById(R.id.jmlTransaksi);

                        penitipan.setText(data.getString("penitipan"));
                        pemesanan.setText(data.getString("pemesanan"));
                        transaksi.setText(data.getString("transaksi"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                dialog.dismiss();
            });
        });
        thread.start();
    }

    private void logout() {
        String url = getString(R.string.api_server) + "/logout";

        Thread thread = new Thread(() -> {
            Http http = new Http(this, url);
            http.setMethod("post");
            http.setToken(true);
            http.send();

            runOnUiThread(() -> {
                JSONObject response = null;
                try {
                    response = new JSONObject(http.getResponse());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int code = http.getStatusCode();
                assert response != null;
                if (code == 200) {
                    try {
                        Toast.makeText(this, response.getString("data"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                localStorage.setToken("");
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        });
        thread.start();
    }
}