package com.example.petshop.transaksi;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;
import static com.example.petshop.pelengkap.Alert.loading;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshop.LoginSignup.LoginActivity;
import com.example.petshop.R;
import com.example.petshop.pelengkap.Http;
import com.example.petshop.pelengkap.LocalStorage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TransaksiLayout extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
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
        String url = getString(R.string.api_server) + "/user/listpembayaran";

        @SuppressLint("NotifyDataSetChanged")
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
                switch (code) {
                    case 200:
                        try {
                            if (!response.getJSONArray("data").toString().equals("[]")) {
                                Gson gson = new Gson();

                                Type dataClassType = new TypeToken<ArrayList<DataClass>>() {}.getType();
                                ArrayList<DataClass> addData = gson.fromJson(response.getJSONArray("data").toString(), dataClassType);

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
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                    case 401:
                        try {
                            kode401(response.getString("message"), this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        localStorage.setToken("");
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();

                        break;
                    default:
                        try {
                            alertFail(response.getString("message"), this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        break;
                }
                dialog.dismiss();
            });
        });
        thread.start();
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