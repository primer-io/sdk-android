package io.primer.android.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.analytics.domain.AnalyticsInteractor

internal class BaseViewModelFactory(private val analyticsInteractor: AnalyticsInteractor) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        BaseViewModel(
            analyticsInteractor,
        ) as T
}
