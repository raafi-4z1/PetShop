package com.example.petshop.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.petshop.data.local.LocalStorage;

public class MainViewModelFactory implements ViewModelProvider.Factory {
    private final LocalStorage local;

    public MainViewModelFactory(LocalStorage local) {
        this.local = local;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(local);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}