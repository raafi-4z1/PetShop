package com.example.petshop.ui.viewmodel.history;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petshop.ui.LoginSignup.LoginActivity;
import com.example.petshop.data.PetshopCallback;
import com.example.petshop.data.local.LocalStorage;
import com.example.petshop.data.repository.MainRepository;
import com.example.petshop.ui.history.DataHistory;
import com.example.petshop.ui.history.HistoryAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class HistoryViewModel extends ViewModel {
    public Boolean isLoad, isPemesanan;
    private MutableLiveData<JSONObject> _result = new MutableLiveData<>();
    private final MainRepository _mainRepository = new MainRepository();
    private final LocalStorage _local;
    private final Gson _gson = new Gson();

    public HistoryViewModel(LocalStorage local) {
        this._local = local;
        this.isLoad = true;
    }

    public void getDataRemote(Context context, String url, String method,
                              Boolean token, String data, AlertDialog dialog) {
        _mainRepository.remoteRepository(url, method, _local, token, data,
                new PetshopCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        _result.postValue(result);
                        if (!isPemesanan)
                            dismissLoading(dialog);
                    }

                    @Override
                    public void onFailure(String message) {
                        dismissLoading(dialog);
                        new Handler(Looper.getMainLooper()).post(() -> alertFail(message, context));
                    }

                    @Override
                    public void dismissLoading(AlertDialog dialog) {
                        if (dialog != null)
                            new Handler(Looper.getMainLooper()).post(dialog::dismiss);
                    }
        });
    }

    public MutableLiveData<JSONObject> getResult() {
        return _result;
    }

    public void handleResult(Context context, JSONObject result, Integer code, HistoryAdapter historyAdapter) {
        try {
            switch (code) {
                case 200:
                case 204:
                    historyAdapter.addSingleHistory(new DataHistory(
                            isPemesanan? "penanda" : null,
                            isPemesanan? null : "penanda",
                            null,
                            "penanda",
                            null,
                            null,
                            null
                    ));

                    if (code != 204) {
                        String data = result.getJSONArray("data").toString();
                        Type dataClassType = new TypeToken<ArrayList<DataHistory>>() {}.getType();
                        ArrayList<DataHistory> addData = _gson.fromJson(data, dataClassType);
                        historyAdapter.addAllHistory(addData);
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
                    kode401(result.getString("message"), context);
                    _local.setToken("");
                    context.startActivity(new Intent(context, LoginActivity.class));
                    ((AppCompatActivity) context).finish();
                    break;
                default:
                    alertFail(result.getString("message"), context);
                    break;
            }
        } catch (JSONException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}