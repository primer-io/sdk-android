package io.primer.sample.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.sample.datamodels.AppLinkParams
import io.primer.sample.datasources.ApiKeyDataSource
import io.primer.sample.repositories.CountryRepository
import java.lang.ref.WeakReference

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(
    private val contextRef: WeakReference<Context>,
    private val countryRepository: CountryRepository,
    private val apiKeyDataSource: ApiKeyDataSource,
    private val appLinkParams: AppLinkParams?
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return MainViewModel(
            contextRef = contextRef,
            countryRepository = countryRepository,
            apiKeyDataSource = apiKeyDataSource,
            savedStateHandle = extras.createSavedStateHandle().apply {
                set("token", appLinkParams?.clientToken)
                set("settings", appLinkParams?.settings)
            }
        ) as T
    }
}
