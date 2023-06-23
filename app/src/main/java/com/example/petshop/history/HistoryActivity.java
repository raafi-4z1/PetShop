package com.example.petshop.history;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
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
    private LocalStorage localStorage;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_history);

        localStorage = new LocalStorage(this);
        TextView nameProfile = findViewById(R.id.txtNameUser);
        nameProfile.setText(localStorage.getNama());

        RecyclerView recyclerView = findViewById(R.id.recycler_view_history);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        historyAdapter = new HistoryAdapter(this);
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
        String url = getString(R.string.api_server) + "/user/viewpemesanan";

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
                            String data = response.getJSONArray("data").toString();
                            historyAdapter.addSingleHistory(new DataHistory(
                                    "penanda",
                                    null,
                                    null,
                                    "penanda",
                                    null
                            ));

                            if (!data.equals("[]")) {
                                Gson gson = new Gson();

                                Type dataClassType = new TypeToken<ArrayList<DataHistory>>() {}.getType();
                                ArrayList<DataHistory> addData = gson.fromJson(data, dataClassType);

                                historyAdapter.addAllHistory(addData);
                            } else {
                                historyAdapter.addSingleHistory(new DataHistory(
                                        null,
                                        null,
                                        null,
                                        "Tidak ada data",
                                        "kosong"
                                ));
                            }

                            getPenitipan();
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
            });
        });
        thread.start();
    }

    private void getPenitipan() {
        String url = getString(R.string.api_server) + "/user/viewpenitipan";

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
                            String data = response.getJSONArray("data").toString();
                            historyAdapter.addSingleHistory(new DataHistory(
                                    null,
                                    "penanda",
                                    null,
                                    "penanda",
                                    null
                            ));

                            if (!data.equals("[]")) {
                                Gson gson = new Gson();

                                Type dataClassType = new TypeToken<ArrayList<DataHistory>>() {}.getType();
                                ArrayList<DataHistory> addData = gson.fromJson(data, dataClassType);

                                historyAdapter.addAllHistory(addData);
                            } else {
                                historyAdapter.addSingleHistory(new DataHistory(
                                        null,
                                        null,
                                        null,
                                        "Tidak ada data",
                                        "kosong"
                                ));
                            }

                            getTransaksi();
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
                            String data = response.getJSONArray("data").toString();
                            historyAdapter.addSingleHistory(new DataHistory(
                                    null,
                                    null,
                                    "penanda",
                                    "penanda",
                                    null
                            ));

                            if (!data.equals("[]")) {
                                Gson gson = new Gson();

                                Type dataClassType = new TypeToken<ArrayList<DataHistory>>() {}.getType();
                                ArrayList<DataHistory> addData = gson.fromJson(data, dataClassType);

                                historyAdapter.addAllHistory(addData);
                            } else {
                                historyAdapter.addSingleHistory(new DataHistory(
                                        null,
                                        null,
                                        null,
                                        "Tidak ada data",
                                        "kosong"
                                ));
                            }

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
            });
        });
        thread.start();
    }

}