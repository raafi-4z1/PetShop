package com.example.petshop.ui.viewmodel;

import static com.example.petshop.pelengkap.Alert.alertFail;

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
import com.example.petshop.ui.profile.ProfileActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class MainViewModel extends ViewModel {
    public Boolean isLoad;
    private MutableLiveData<JSONObject> _result;
    private final MainRepository mainRepository;
    private final LocalStorage local;

    public MainViewModel(LocalStorage local) {
        this.local = local;
        this.isLoad = true;
        _result = new MutableLiveData<>();
        mainRepository = new MainRepository();
    }

    public void setDataRemote(Context context, String url, String method,
                              Boolean token, String data, AlertDialog dialog) {
        mainRepository.remoteRepository(url, method, local, token, data,
                new PetshopCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        _result.postValue(result);
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

    public void handleResult(Context context, JSONObject result, Integer code, String methodName) {
        try {
            if (code == 401) {
                local.setToken("");
                context.startActivity(new Intent(context, LoginActivity.class));
                ((AppCompatActivity) context).finish();
            } else if (methodName.equals("logout")) {
                if (code == 200) {
                    Toast.makeText(context, result.getString("data"), Toast.LENGTH_LONG).show();
                } else {
                    alertFail(result.getString("message"), context);
                }

                local.setToken("");
                context.startActivity(new Intent(context, LoginActivity.class));
                ((AppCompatActivity) context).finish();
            } else if (code == 200 && methodName.equals("home")) {
                JSONObject data = result.getJSONObject("data");

                local.setNama(data.getString("nama_lengkap"));
                local.setTelepon(data.getString("telepon"));
                local.setEmail(data.getString("email"));
                local.setAlamat(data.getString("alamat"));

                if (data.getString("alamat").isEmpty())
                    new AlertDialog.Builder(context)
                            .setTitle("Konfirmasi")
                            .setMessage("Apakah anda ingin update Profile?")
                            .setPositiveButton("OK", (dialogInterface, which)
                                    -> context.startActivity(new Intent(context, ProfileActivity.class)))
                            .setNegativeButton("Tidak", (dialogInterface, i) -> {
                            })
                            .show();
            } else {
                alertFail(result.getString("message"), context);
            }
        } catch (JSONException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}