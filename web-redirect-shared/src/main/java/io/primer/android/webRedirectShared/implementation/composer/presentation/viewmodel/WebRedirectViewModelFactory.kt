package io.primer.android.webRedirectShared.implementation.composer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor

internal class WebRedirectViewModelFactory(
    private val analyticsInteractor: AnalyticsInteractor,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        return WebRedirectViewModel(
            analyticsInteractor = analyticsInteractor,
        ) as T
    }
}
