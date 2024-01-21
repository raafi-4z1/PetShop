package com.example.petshop.data.local;

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
        return sharedPreferences.getString("sesi_berakhir", "");
    }

    public void setSesi(String sesi_berakhir) {
        editor.putString("sesi_berakhir", sesi_berakhir).commit();
    }

    public void setRemember(Boolean value) {
        editor.putBoolean("remember", value).commit();
    }

    public Boolean getRemember() { return sharedPreferences.getBoolean("remember", false); }

    public void setKey(String value) { editor.putString("petshop_key_store", value).commit(); }

    public String getKey() { return sharedPreferences.getString("petshop_key_store", "null"); }

    public void setUsername(String value) { editor.putString("username", value).commit(); }

    public String getUsername() { return sharedPreferences.getString("username", ""); }

    public void setPassword(String value) { editor.putString("password", value).commit(); }

    public String getPassword() { return sharedPreferences.getString("password", ""); }
}