package com.example.myapplication.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.myapplication.datasources.ApiKeyDataSource

class HeadlessManagerViewModelFactory(
    private val apiKeyDataSource: ApiKeyDataSource,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return HeadlessManagerViewModel(
            apiKeyDataSource,
            extras.createSavedStateHandle()
        ) as T
    }
}
