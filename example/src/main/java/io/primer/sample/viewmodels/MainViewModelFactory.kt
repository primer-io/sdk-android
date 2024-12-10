package io.primer.sample.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.sample.datasources.ApiKeyDataSource
import io.primer.sample.repositories.CountryRepository
import java.lang.ref.WeakReference

class MainViewModelFactory(
    private val contextRef: WeakReference<Context>,
    private val countryRepository: CountryRepository,
    private val apiKeyDataSource: ApiKeyDataSource,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return MainViewModel(
            contextRef = contextRef,
            countryRepository = countryRepository,
            apiKeyDataSource = apiKeyDataSource,
            savedStateHandle = extras.createSavedStateHandle(),
            application = application
        ) as T
    }
}
