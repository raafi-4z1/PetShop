package com.example.petshop.ui.transaksi;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;
import static com.example.petshop.pelengkap.Alert.loading;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshop.R;
import com.example.petshop.data.PetshopCallback;
import com.example.petshop.data.local.LocalStorage;
import com.example.petshop.data.repository.MainRepository;
import com.example.petshop.ui.LoginSignup.LoginActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TransaksiLayout extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    private MainRepository mainRepository;
    private ArrayList<DataClass> dataList;
    private LocalStorage localStorage;
    private Adapter adapter = null;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.transaksi_layout);

        dialog = loading(TransaksiLayout.this);
        dialog.show();

        mainRepository = new MainRepository();
        localStorage = new LocalStorage(this);
        TextView nameProfile = findViewById(R.id.txtNameUser);
        nameProfile.setText(localStorage.getNama());

        RecyclerView recyclerView = findViewById(R.id.recycler_view_list_pembayaran);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        dataList = new ArrayList<>();
        adapter = new Adapter(this, dataList, REQUEST_CODE);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.transaksi_back_button).setOnClickListener(view -> finish());
        getData();
    }

    private void getData() {
        mainRepository.remoteRepository("user/listpembayaran", "post", localStorage,
                true, null,
                new PetshopCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        try {
                            int code = Integer.parseInt(result.getString("code"));
                            switch (code) {
                                case 200:
                                    if (!result.getJSONArray("data").toString().equals("[]")) {
                                        Gson gson = new Gson();
                                        Type dataClassType = new TypeToken<ArrayList<DataClass>>() {}.getType();
                                        ArrayList<DataClass> addData = gson.fromJson(result.getJSONArray("data").toString(), dataClassType);

                                        dataList.addAll(addData);
                                    } else {
                                        dataList.add(new DataClass(
                                                "Tidak ada data",
                                                null,
                                                null,
                                                null,
                                                null
                                        ));
                                    }
                                    new Handler(Looper.getMainLooper()).post(() -> adapter.notifyDataSetChanged());
                                    break;
                                case 401:
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        try {
                                            kode401(result.getString("message"), TransaksiLayout.this);
                                            localStorage.setToken("");
                                            startActivity(new Intent(TransaksiLayout.this, LoginActivity.class));
                                            finish();
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                    break;
                                default:
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        try {
                                            alertFail(result.getString("message"), TransaksiLayout.this);
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                    break;
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        dismissLoading(dialog);
                    }

                    @Override
                    public void onFailure(String message) {
                        dismissLoading(dialog);
                        new Handler(Looper.getMainLooper()).post(() -> alertFail(message, TransaksiLayout.this));
                    }

                    @Override
                    public void dismissLoading(AlertDialog dialog) {
                        if (dialog != null)
                            new Handler(Looper.getMainLooper()).post(dialog::dismiss);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshActivity();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshActivity() {
        dialog.show();
        dataList.clear();
        adapter.notifyDataSetChanged();
        getData();
    }
}