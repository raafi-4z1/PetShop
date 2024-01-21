package com.example.petshop.ui.profile;

import static com.example.petshop.pelengkap.Alert.loading;
import static com.example.petshop.pelengkap.StringPhone.formatPhone;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.petshop.ui.viewmodel.MainViewModel;
import com.example.petshop.ui.viewmodel.MainViewModelFactory;
import com.example.petshop.R;
import com.example.petshop.data.local.LocalStorage;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {
    private MainViewModel mainViewModel;
    private AlertDialog dialog;
    private LocalStorage local;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);

        local = new LocalStorage(this);
        MainViewModelFactory factory = new MainViewModelFactory(local);
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        dialog = loading(ProfileActivity.this);
        bindViews();

        TextView penitipan = findViewById(R.id.jmlPenitipan);
        TextView pemesanan = findViewById(R.id.jmlPemesanan);
        TextView transaksi = findViewById(R.id.jmlTransaksi);

        mainViewModel.getResult().observe(this, result -> {
            if (result != null) {
                try {
                    int code = Integer.parseInt(result.getString("code"));
                    if (code == 200) {
                        JSONObject data = result.getJSONObject("data");

                        penitipan.setText(data.getString("penitipan"));
                        pemesanan.setText(data.getString("pemesanan"));
                        transaksi.setText(data.getString("transaksi"));
                    } else {
                        Toast.makeText(this, result.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        findViewById(R.id.btnChangePassword).setOnClickListener(view
                -> startActivity(new Intent(this, UpdatePasswordActivity.class)));

        findViewById(R.id.btnEditProfile).setOnClickListener(view
                -> startActivity(new Intent(this, UpdateProfileActivity.class)));

        if (mainViewModel.isLoad) {
            dialog.show();
            mainViewModel.isLoad = false;
            getJmlHistory();
        }
    }

    @SuppressLint("SetTextI18n")
    private void bindViews() {
        TextView nama = findViewById(R.id.txtNamaProfile);
        TextView email = findViewById(R.id.txtEmailProfile);
        TextView phone = findViewById(R.id.txtPhoneProfile);
        TextView alamat = findViewById(R.id.txtAlamatProfile);

        nama.setText(local.getNama());
        email.setText(local.getEmail().isEmpty()? "email" : local.getEmail());
        phone.setText("+62 " + formatPhone(local.getTelepon()));
        alamat.setText(local.getAlamat().isEmpty()? "alamat" : local.getAlamat());
    }

    private void getJmlHistory() {
        mainViewModel.setDataRemote(this, "user/profile", "post",
                true, null, dialog);
    }
}