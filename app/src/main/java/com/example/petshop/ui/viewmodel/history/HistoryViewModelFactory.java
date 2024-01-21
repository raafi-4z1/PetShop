package com.example.petshop.ui.viewmodel.history;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.petshop.data.local.LocalStorage;

public class HistoryViewModelFactory implements ViewModelProvider.Factory {
    private final LocalStorage local;

    public HistoryViewModelFactory(LocalStorage local) {
        this.local = local;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HistoryViewModel.class)) {
            return (T) new HistoryViewModel(local);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}