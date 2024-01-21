package com.example.petshop.data;

import android.app.AlertDialog;

public interface PetshopCallback<T> {
    void onSuccess(T result);
    void onFailure(String message);
    void dismissLoading(AlertDialog dialog);
}