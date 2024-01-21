package com.example.petshop.ui.viewmodel.detail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.petshop.data.local.LocalStorage;

public class DetailViewModelFactory implements ViewModelProvider.Factory {
    private final LocalStorage local;

    public DetailViewModelFactory(LocalStorage local) {
        this.local = local;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DetailViewModel.class)) {
            return (T) new DetailViewModel(local);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}