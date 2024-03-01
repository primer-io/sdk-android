package io.primer.sample.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.sample.datasources.ApiKeyDataSource

class HeadlessManagerViewModelFactory(
    private val apiKeyDataSource: ApiKeyDataSource,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return HeadlessManagerViewModel(
            apiKeyDataSource,
            extras.createSavedStateHandle()
        ) as T
    }
}
