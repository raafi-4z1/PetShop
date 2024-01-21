package com.example.petshop.ui.history;

import static com.example.petshop.pelengkap.Alert.loading;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshop.R;
import com.example.petshop.data.local.LocalStorage;
import com.example.petshop.ui.viewmodel.history.HistoryViewModel;
import com.example.petshop.ui.viewmodel.history.HistoryViewModelFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

public class HistoryActivity extends AppCompatActivity implements ItemClickListener {
    private Boolean isHistory = true;
    private String finalData = null;
    private HistoryViewModel historyViewModel;
    private HistoryAdapter historyAdapter;
    private AlertDialog dialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_history);

        dialog = loading(HistoryActivity.this);
        LocalStorage localStorage = new LocalStorage(this);
        HistoryViewModelFactory factory = new HistoryViewModelFactory(localStorage);
        historyViewModel = new ViewModelProvider(this, factory).get(HistoryViewModel.class);

        TextView nameProfile = findViewById(R.id.txtNameUser);
        nameProfile.setText(localStorage.getNama());
        isHistory = getIntent().getBooleanExtra("is_history", true);
        LinearLayout linearLayoutPilihWaktu = findViewById(R.id.linearLayoutPilihWaktu);

        if (!isHistory) {
            TextView judul = findViewById(R.id.judulLayoutHistory);
            TextView deskripsi = findViewById(R.id.deskripsiLayoutHistory);

            judul.setText("Jadwal");
            deskripsi.setText("Jadwal Anda");
            historyAdapter = new HistoryAdapter(this, false);
            linearLayoutPilihWaktu.setVisibility(View.GONE);
        } else {
            selectWaktu();
            historyAdapter = new HistoryAdapter(this, true);
            linearLayoutPilihWaktu.setVisibility(View.VISIBLE);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view_history);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        historyAdapter.setItemClickListener(this);
        recyclerView.setAdapter(historyAdapter);
        historyAdapter.clearData();

        historyViewModel.getResult().observe(this, result -> {
            if (result != null) {
                int code;
                try {
                    code = Integer.parseInt(result.getString("code"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                historyViewModel.handleResult(this, result, code, historyAdapter);
                if (code <= 204 && code >= 200 && historyViewModel.isPemesanan)
                    getPenitipan();
            }
        });

        findViewById(R.id.history_back_button).setOnClickListener(view -> finish());
        if (historyViewModel.isLoad) {
            dialog.show();
            historyViewModel.isLoad = false;
            getPemesanan(ALL);
        }
    }

    private static final int ALL = 0, WEEK1 = 1, WEEKS2 = 2, WEEKS3 = 3, WEEKS4 = 4;

    private void selectWaktu() {
        String[] item = {"all", "1 week before", "2 weeks before", "3 weeks before", "4 weeks before"};
        AutoCompleteTextView autoCompleteTextView;
        ArrayAdapter<String> adapterItems;

        autoCompleteTextView = findViewById(R.id.autoCompleteTxt);
        adapterItems = new ArrayAdapter<>(this, R.layout.list_item_history, item);
        autoCompleteTextView.setAdapter(adapterItems);

        autoCompleteTextView.setOnItemClickListener((adapterView, view, i, l) -> getPemesanan(i));
    }

    private String getTanggal(int i) {
        String hasil = "all";

        switch (i) {
            case ALL:
                hasil = "all";
                break;
            case WEEK1:
                hasil = LocalDateTime.now().minusWeeks(WEEK1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                break;
            case WEEKS2:
                hasil = LocalDateTime.now().minusWeeks(WEEKS2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                break;
            case WEEKS3:
                hasil = LocalDateTime.now().minusWeeks(WEEKS3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                break;
            case WEEKS4:
                hasil = LocalDateTime.now().minusWeeks(WEEKS4).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                break;
        }

        return hasil;
    }

    @Override
    public void onItemClick(int position) {
        Log.d("TAG", "onItemClick: klik RecyclerView");
    }

    private void getPemesanan(int tanggalTerpilih) {
        String url, dataParams = null;

        if (isHistory) {
            JSONObject params = new JSONObject();
            try {
                params.put("tanggal_terpilih", getTanggal(tanggalTerpilih));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dataParams = params.toString();

            url = "user/viewpemesanan";
        } else {
            url = "user/viewjadwalpemesanan";
        }

        finalData = dataParams;
        historyViewModel.isPemesanan = true;
        historyViewModel.getDataRemote(this, url, "post", true, finalData, dialog);
    }

    private void getPenitipan() {
        String url;
        if (isHistory) {
            url = "user/viewpenitipan";
        } else {
            url = "user/viewjadwalpenitipan";
        }

        historyViewModel.isPemesanan = false;
        historyViewModel.getDataRemote(this, url, "post", true, finalData, dialog);
    }
}