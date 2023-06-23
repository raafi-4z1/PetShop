package com.example.petshop.pelengkap;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorage {
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public LocalStorage(Context context) {
        sharedPreferences = context.getSharedPreferences("STORAGE_LOGIN_API",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public String getToken() {
        return sharedPreferences.getString("access_token", "");
    }

    public void setToken(String token) {
        editor.putString("access_token", token).commit();
    }

    public String getNama() {
        return sharedPreferences.getString("nama", "");
    }

    public void setNama(String nama) {
        editor.putString("nama", nama).commit();
    }

    public String getTelepon() {
        return sharedPreferences.getString("telepon", "");
    }

    public void setTelepon(String telepon) {
        editor.putString("telepon", telepon).commit();
    }

    public String getEmail() {
        return sharedPreferences.getString("email", "");
    }

    public void setEmail(String email) {
        editor.putString("email", email).commit();
    }

    public String getAlamat(){
        return sharedPreferences.getString("alamat", "");
    }

    public void setAlamat(String alamat) {
        editor.putString("alamat", alamat).commit();
    }

    public String getSesi() {
        return sharedPreferences.getString("sesiBerakhir", "");
    }

    public void setSesi(String sesi_berakhir) {
        editor.putString("sesiBerakhir", sesi_berakhir).commit();
    }
}
