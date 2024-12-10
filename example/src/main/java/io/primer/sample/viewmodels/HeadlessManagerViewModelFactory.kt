package io.primer.sample.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.sample.datasources.ApiKeyDataSource

class HeadlessManagerViewModelFactory(
    private val apiKeyDataSource: ApiKeyDataSource,
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return HeadlessManagerViewModel(
            apiKeyDataSource = apiKeyDataSource,
            savedStateHandle = extras.createSavedStateHandle(),
            application = application
        ) as T
    }
}
