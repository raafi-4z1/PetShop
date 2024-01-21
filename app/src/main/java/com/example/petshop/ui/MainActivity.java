package com.example.petshop.ui;

import static com.example.petshop.pelengkap.Alert.loading;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.petshop.R;
import com.example.petshop.data.local.LocalStorage;
import com.example.petshop.ui.history.HistoryActivity;
import com.example.petshop.ui.pemesanan.PemesananActivity;
import com.example.petshop.ui.penitipan.PenitipanActivity;
import com.example.petshop.ui.profile.ProfileActivity;
import com.example.petshop.ui.transaksi.TransaksiLayout;
import com.example.petshop.ui.viewmodel.MainViewModel;
import com.example.petshop.ui.viewmodel.MainViewModelFactory;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    private MainViewModel mainViewModel;
    private AlertDialog dialog;
    private String methodName = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        LocalStorage local = new LocalStorage(this);
        MainViewModelFactory factory = new MainViewModelFactory(local);
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        TextView inpName = findViewById(R.id.txtNameUser);
        dialog = loading(this);

        mainViewModel.getResult().observe(this, result -> {
            if (result != null) {
                try {
                    int code = Integer.parseInt(result.getString("code"));

                    mainViewModel.handleResult(this, result, code, methodName);
                    if (code == 200 && methodName.equals("home"))
                        inpName.setText(result.getJSONObject("data").getString("nama_lengkap"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        clickLayout();
        if (mainViewModel.isLoad) {
            dialog.show();
            loadDataHome();
            mainViewModel.isLoad = false;
        }
    }

    private void clickLayout() {
        findViewById(R.id.cardViewPemesanan).setOnClickListener(view
                -> startActivity(new Intent(getApplicationContext(), PemesananActivity.class)));

        findViewById(R.id.cardViewPenitipan).setOnClickListener(view
                -> startActivity(new Intent(getApplicationContext(), PenitipanActivity.class)));

        findViewById(R.id.cardViewTransaksi).setOnClickListener(view
                -> startActivity(new Intent(getApplicationContext(), TransaksiLayout.class)));

        findViewById(R.id.cardViewJadwal).setOnClickListener(view
                -> startActivity(new Intent(getApplicationContext(), HistoryActivity.class)
                .putExtra("is_history", false)));

        findViewById(R.id.cardViewHistory).setOnClickListener(view
                -> startActivity(new Intent(getApplicationContext(), HistoryActivity.class)
                .putExtra("is_history", true)));

        findViewById(R.id.cardViewLogoutDasboard).setOnClickListener(view
                -> new AlertDialog.Builder(MainActivity.this)
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda ingin keluar dari aplikasi?")
                .setPositiveButton("OK", (dialogInterface, which)
                        -> logout())
                .setNegativeButton("Tidak", (dialogInterface, i) -> {})
                .show());

        findViewById(R.id.cardViewUser).setOnClickListener(view
                -> startActivity(new Intent(getApplicationContext(), ProfileActivity.class)));
    }

    private void logout() {
        mainViewModel.setDataRemote(this, "logout", "post",
                true, null, dialog);
        methodName = "logout";
    }

    private void loadDataHome() {
        mainViewModel.setDataRemote(this, "user/home", "post",
                true, null, dialog);
    }
}