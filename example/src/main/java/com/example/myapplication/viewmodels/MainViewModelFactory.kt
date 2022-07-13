package com.example.myapplication.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.datasources.ApiKeyDataSource
import com.example.myapplication.repositories.CountryRepository
import java.lang.ref.WeakReference

class MainViewModelFactory(
    private val contextRef: WeakReference<Context>,
    private val countryRepository: CountryRepository,
    private val apiKeyDataSource: ApiKeyDataSource,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(
        contextRef,
        countryRepository,
        apiKeyDataSource
    ) as T
}
