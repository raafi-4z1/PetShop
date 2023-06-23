package com.example.petshop;

import static android.content.ContentValues.TAG;
import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petshop.LoginSignup.LoginActivity;
import com.example.petshop.LoginSignup.SignUpActivity;
import com.example.petshop.history.HistoryActivity;
import com.example.petshop.pelengkap.Http;
import com.example.petshop.pelengkap.LocalStorage;
import com.example.petshop.pemesanan.PemesananActivity;
import com.example.petshop.penitipan.PenitipanActivity;
import com.example.petshop.profile.ProfileActivity;
import com.example.petshop.transaksi.TransaksiLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        findViewById(R.id.cardViewPemesanan).setOnClickListener(view 
                -> startActivity(new Intent(getApplicationContext(), PemesananActivity.class)));

        findViewById(R.id.cardViewPenitipan).setOnClickListener(view 
                -> startActivity(new Intent(getApplicationContext(), PenitipanActivity.class)));

        findViewById(R.id.cardViewTransaksi).setOnClickListener(view 
                -> startActivity(new Intent(getApplicationContext(), TransaksiLayout.class)));

        findViewById(R.id.cardViewHistory).setOnClickListener(view 
                -> startActivity(new Intent(getApplicationContext(), HistoryActivity.class)));

        findViewById(R.id.cardViewLogoutDasboard).setOnClickListener(view 
                -> new AlertDialog.Builder(MainActivity.this)
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda ingin keluar dari aplikasi?")
                .setPositiveButton("OK", (dialog, which)
                        -> logout())
                .setNegativeButton("Tidak", (dialog, i) -> {})
                .show());

        findViewById(R.id.cardViewUser).setOnClickListener(view
                -> startActivity(new Intent(getApplicationContext(), ProfileActivity.class)));

        getHome();
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

                LocalStorage local = new LocalStorage(this);
                local.setToken("");
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        });
        thread.start();
    }

    private void getHome() {
        String url = getString(R.string.api_server) + "/user/home";

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
                LocalStorage local = new LocalStorage(this);

                int code = http.getStatusCode();
                switch (code) {
                    case 200:
                        try {
                            assert response != null;
                            response = response.getJSONObject("data");
                            TextView inpName = findViewById(R.id.txtNameUser);
                            inpName.setText(response.getString("nama_lengkap"));

                            local.setNama(response.getString("nama_lengkap"));
                            local.setTelepon(response.getString("telepon"));
                            local.setEmail(response.getString("email"));
                            local.setAlamat(response.getString("alamat"));

                            if (response.getString("alamat").isEmpty())
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Konfirmasi")
                                        .setMessage("Apakah anda ingin update Profile?")
                                        .setPositiveButton("OK", (dialog, which)
                                                -> startActivity(new Intent(getApplicationContext(), ProfileActivity.class)))
                                        .setNegativeButton("Tidak", (dialog, i) -> {})
                                        .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                    case 401:
                        try {
                            kode401(response != null ? response.getString("message") : null, this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        local.setToken("");
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();

                        break;
                    default:
                        try {
                            alertFail(response != null ? response.getString("message") : null, this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        break;
                }
            });
        });
        thread.start();
    }
}