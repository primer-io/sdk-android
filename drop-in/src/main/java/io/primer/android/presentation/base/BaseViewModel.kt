package io.primer.android.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import kotlinx.coroutines.launch

internal open class BaseViewModel(private val analyticsInteractor: AnalyticsInteractor) :
    ViewModel() {
    fun addAnalyticsEvent(params: BaseAnalyticsParams) =
        viewModelScope.launch {
            analyticsInteractor(params)
        }
}
