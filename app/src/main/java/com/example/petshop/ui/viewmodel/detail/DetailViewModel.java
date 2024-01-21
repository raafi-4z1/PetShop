package com.example.petshop.ui.viewmodel.detail;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;
import static com.example.petshop.pelengkap.DateValidator.convertDateFormat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petshop.data.PetshopCallback;
import com.example.petshop.data.local.LocalStorage;
import com.example.petshop.data.repository.MainRepository;
import com.example.petshop.ui.LoginSignup.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailViewModel extends ViewModel {
    public Boolean isLoad;
    private MutableLiveData<JSONObject> _result;
    private final MainRepository mainRepository;
    private final LocalStorage local;

    public DetailViewModel(LocalStorage local) {
        this.local = local;
        this.isLoad = true;
        _result = new MutableLiveData<>();
        mainRepository = new MainRepository();
    }

    public void setDataRemote(Context context, String url, String method,
                              Boolean token, String data) {
        mainRepository.remoteRepository(url, method, local, token, data,
                new PetshopCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        handleResult(context, result);
                    }

                    @Override
                    public void onFailure(String message) {
                        new Handler(Looper.getMainLooper()).post(() -> alertFail(message, context));
                    }

                    @Override
                    public void dismissLoading(AlertDialog dialog) { }
        });
    }

    public void setTransaksi(Context context, String url, String method,
                              Boolean token, String data, Boolean isTransaksi) {
        mainRepository.remoteRepository(url, method, local, token, data,
                new PetshopCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        handleTransaksi(context, result, isTransaksi);
                    }

                    @Override
                    public void onFailure(String message) {
                        new Handler(Looper.getMainLooper()).post(() -> alertFail(message, context));
                    }

                    @Override
                    public void dismissLoading(AlertDialog dialog) { }
                });
    }

    private void handleTransaksi(Context context, JSONObject result, Boolean isTransaksi) {
        try {
            int code = Integer.parseInt(result.getString("code"));
            switch (code) {
                case 200:
                    if (!isTransaksi) {
                        Toast.makeText(context, result.getString("data"), Toast.LENGTH_LONG).show();
                    } else
                        Log.d("TAG, updateTransaksi: ", result.getString("data"));
                    break;
                case 401:
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            kode401(result.getString("message"), context);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        local.setToken("");
                        context.startActivity(new Intent(context, LoginActivity.class));
                        ((AppCompatActivity) context).finish();
                    });
                    break;
                default:
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            alertFail(result.getString("message"), context);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    break;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public MutableLiveData<JSONObject> getResult() {
        return _result;
    }

    public void handleResult(Context context, JSONObject result) {
        try {
            int code = Integer.parseInt(result.getString("code"));
            switch (code) {
                case 200:
                    JSONObject data = result.getJSONObject("data");
                    String namaPemesan, emailPemesan, phonePemesan, alamatPemesan, valJasaBayar,
                            tglBayar = " ", va, isPemesanan;

                    namaPemesan = data.getString("nama_lengkap").equals("null") ? "-" : data.getString("nama_lengkap");
                    emailPemesan = data.getString("email").equals("null") ? "-" : data.getString("email");
                    phonePemesan = data.getString("telepon").equals("null") ? "-" : data.getString("telepon");
                    alamatPemesan = data.getString("alamat").equals("null") ? "-" : data.getString("alamat");

                    valJasaBayar = data.getString("jenis_pembayaran").equals("null") ? "-" : data.getString("jenis_pembayaran");
                    if (data.getString("status").equals("SUCCESS")) {
                        tglBayar = convertDateFormat(data.getString("tanggal_bayar"),
                                "yyyy-MM-dd HH:mm:ss", "EEEE, d MMMM yyyy - HH:mm");
                    }
                    va = data.getString("va_number").equals("null") ? "-" : data.getString("va_number");
                    isPemesanan = data.getString("pemesanan");

                    JSONObject value = new JSONObject();
                    value.put("namaHewan", data.getString("nama_hewan"));
                    value.put("jenisHewan", data.getString("jenis"));
                    value.put("jumlahHewan", data.getString("jumlah"));
                    value.put("hargaHewan", data.getString("harga"));

                    value.put("namaPemesan", namaPemesan);
                    value.put("emailPemesan", emailPemesan);
                    value.put("phonePemesan", phonePemesan);
                    value.put("alamatPemesan", alamatPemesan);

                    value.put("valJasaBayar", valJasaBayar);
                    value.put("stsPesRes", data.getString("status_pesan"));
                    value.put("statusRes", data.getString("status"));
                    value.put("tglBayar", tglBayar);
                    value.put("va", va);
                    value.put("isPemesanan", isPemesanan);

                    if (isPemesanan.equals("true")) {
                        value.put("tglPesan", data.getString("tanggal_pemesanan"));
                    } else {
                        value.put("tglMasRes", data.getString("tanggal_masuk"));
                        value.put("tglKelRes", data.getString("tanggal_keluar"));
                    }
                    _result.postValue(value);
                    break;
                case 204:
                    new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context,
                                "Ada kesalahan sistem atau data dalam penyimpanan tidak lengkap",
                                Toast.LENGTH_LONG).show());
                    break;
                case 401:
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            kode401(result.getString("message"), context);
                        } catch (JSONException ex) {
                            throw new RuntimeException(ex);
                        }
                        local.setToken("");
                        context.startActivity(new Intent(context, LoginActivity.class));
                        ((AppCompatActivity) context).finish();
                    });
                    break;
                default:
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            alertFail(result.getString("message"), context);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    break;
            }
        } catch (JSONException e) {
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}