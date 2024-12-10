package io.primer.android.webRedirectShared.implementation.composer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import kotlinx.coroutines.launch

internal class WebRedirectViewModel(
    private val analyticsInteractor: AnalyticsInteractor
) : ViewModel() {

    fun addAnalyticsEvent(params: BaseAnalyticsParams) = viewModelScope.launch {
        analyticsInteractor(params)
    }
}
