package com.example.myapplication.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.myapplication.datasources.ApiKeyDataSource
import com.example.myapplication.repositories.CountryRepository
import java.lang.ref.WeakReference

class MainViewModelFactory(
    private val contextRef: WeakReference<Context>,
    private val countryRepository: CountryRepository,
    private val apiKeyDataSource: ApiKeyDataSource,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return MainViewModel(
            contextRef,
            countryRepository,
            apiKeyDataSource,
            extras.createSavedStateHandle()
        ) as T
    }
}
