package com.example.petshop.data.repository;

import com.example.petshop.data.PetshopCallback;
import com.example.petshop.data.local.LocalStorage;
import com.example.petshop.data.remote.Http;
import com.example.petshop.pelengkap.Credentials;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainRepository {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Http http;

    public MainRepository() {
        http = new Http();
    }

    public void remoteRepository(String url, String method,
                                 LocalStorage localStorage, Boolean token, String data,
                                 PetshopCallback<JSONObject> callback) {
        executorService.execute(() -> {
            try {
                http.setUrl(Credentials.BASE_URL + url);
                http.setMethod(method);
                if (token) http.setToken(true, localStorage);
                if (data != null) http.setData(data);
                http.send();
                JSONObject response;
                if (!http.getResponse().isEmpty()) {
                    response = new JSONObject(http.getResponse());
                } else
                    response = new JSONObject("{code: " + http.getStatusCode() + "}");

                callback.onSuccess(response);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onFailure("Gagal Memuat Data");
            }
        });
    }
}