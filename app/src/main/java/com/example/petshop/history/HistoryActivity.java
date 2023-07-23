package com.example.petshop.history;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;
import static com.example.petshop.pelengkap.Alert.loading;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

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

public class HistoryActivity extends AppCompatActivity implements ItemClickListener {
    private Boolean isHistory = true;
    private LocalStorage localStorage;
    private HistoryAdapter historyAdapter;
    private AlertDialog dialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_history);

        dialog = loading(HistoryActivity.this);
        dialog.show();

        localStorage = new LocalStorage(this);
        TextView nameProfile = findViewById(R.id.txtNameUser);
        nameProfile.setText(localStorage.getNama());
        isHistory = getIntent().getBooleanExtra("is_history", true);

        if (!isHistory) {
            TextView judul = findViewById(R.id.judulLayoutHistory);
            TextView deskripsi = findViewById(R.id.deskripsiLayoutHistory);

            judul.setText("Jadwal");
            deskripsi.setText("Jadwal Anda");
            historyAdapter = new HistoryAdapter(this, false);
        } else {
            historyAdapter = new HistoryAdapter(this, true);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view_history);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        historyAdapter.setItemClickListener(this);
        recyclerView.setAdapter(historyAdapter);

        findViewById(R.id.history_back_button).setOnClickListener(view
                -> finish());

        getPemesanan();
    }

    @Override
    public void onItemClick(int position) {
        Log.d("TAG", "onItemClick: klik RecyclerView");
    }

    private void getPemesanan() {
        String url;
        if (isHistory) {
            url = getString(R.string.api_server) + "/user/viewpemesanan";
        }
        else {
            url = getString(R.string.api_server) + "/user/viewjadwalpemesanan";
        }

        @SuppressLint("NotifyDataSetChanged")
        Thread thread = new Thread(() -> {
            Http http = new Http(this, url);
            http.setMethod("post");
            http.setToken(true);
            http.send();

            runOnUiThread(() -> {
                int code = http.getStatusCode();
                switch (code) {
                    case 200:
                    case 204:
                        historyAdapter.addSingleHistory(new DataHistory(
                                "penanda",
                                null,
                                null,
                                "penanda",
                                null,
                                null,
                                null
                        ));

                        if (code != 204) {
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String data = response.getJSONArray("data").toString();
                                Gson gson = new Gson();

                                Type dataClassType = new TypeToken<ArrayList<DataHistory>>() {}.getType();
                                ArrayList<DataHistory> addData = gson.fromJson(data, dataClassType);

                                historyAdapter.addAllHistory(addData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            historyAdapter.addSingleHistory(new DataHistory(
                                    null,
                                    null,
                                    null,
                                    "Tidak ada data",
                                    null,
                                    null,
                                    "kosong"
                            ));
                        }

                        getPenitipan();

                        break;
                    case 401:
                        try {
                            kode401(new JSONObject(http.getResponse()).getString("message"), this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        localStorage.setToken("");
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();

                        break;
                    default:
                        try {
                            alertFail(new JSONObject(http.getResponse()).getString("message"), this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        break;
                }
            });
        });
        thread.start();
    }

    private void getPenitipan() {
        String url;
        if (isHistory) {
            url = getString(R.string.api_server) + "/user/viewpenitipan";
        } else {
            url = getString(R.string.api_server) + "/user/viewjadwalpenitipan";
        }

        @SuppressLint("NotifyDataSetChanged")
        Thread thread = new Thread(() -> {
            Http http = new Http(this, url);
            http.setMethod("post");
            http.setToken(true);
            http.send();

            runOnUiThread(() -> {
                int code = http.getStatusCode();
                switch (code) {
                    case 200:
                    case 204:
                        historyAdapter.addSingleHistory(new DataHistory(
                                null,
                                "penanda",
                                null,
                                "penanda",
                                null,
                                null,
                                null
                        ));

                        if (code != 204) {
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String data = response.getJSONArray("data").toString();
                                Gson gson = new Gson();

                                Type dataClassType = new TypeToken<ArrayList<DataHistory>>() {}.getType();
                                ArrayList<DataHistory> addData = gson.fromJson(data, dataClassType);

                                historyAdapter.addAllHistory(addData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            historyAdapter.addSingleHistory(new DataHistory(
                                    null,
                                    null,
                                    null,
                                    "Tidak ada data",
                                    null,
                                    null,
                                    "kosong"
                            ));
                        }

                        if (isHistory) {
                            getTransaksi();
                        } else
                            dialog.dismiss();

                        break;
                    case 401:
                        try {
                            kode401(new JSONObject(http.getResponse()).getString("message"), this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        localStorage.setToken("");
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();

                        break;
                    default:
                        try {
                            alertFail(new JSONObject(http.getResponse()).getString("message"), this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        break;
                }
            });
        });
        thread.start();
    }

    private void getTransaksi() {
        String url = getString(R.string.api_server) + "/user/viewtransaksi";

        @SuppressLint("NotifyDataSetChanged")
        Thread thread = new Thread(() -> {
            Http http = new Http(this, url);
            http.setMethod("post");
            http.setToken(true);
            http.send();

            runOnUiThread(() -> {
                int code = http.getStatusCode();
                switch (code) {
                    case 200:
                    case 204:
                        historyAdapter.addSingleHistory(new DataHistory(
                                null,
                                null,
                                "penanda",
                                "penanda",
                                null,
                                null,
                                null
                        ));

                        if (code != 204) {
                            try {
                                JSONObject response = new JSONObject(http.getResponse());
                                String data = response.getJSONArray("data").toString();
                                Gson gson = new Gson();

                                Type dataClassType = new TypeToken<ArrayList<DataHistory>>() {}.getType();
                                ArrayList<DataHistory> addData = gson.fromJson(data, dataClassType);

                                historyAdapter.addAllHistory(addData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            historyAdapter.addSingleHistory(new DataHistory(
                                    null,
                                    null,
                                    null,
                                    "Tidak ada data",
                                    null,
                                    null,
                                    "kosong"
                            ));
                        }

                        break;
                    case 401:
                        try {
                            kode401(new JSONObject(http.getResponse()).getString("message"), this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        localStorage.setToken("");
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();

                        break;
                    default:
                        try {
                            alertFail(new JSONObject(http.getResponse()).getString("message"), this);
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

}