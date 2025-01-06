package io.primer.android.processor3ds.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor

internal class Processor3DSViewModelFactory(
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        return Processor3DSViewModel(
            pollingInteractor = pollingInteractor,
            analyticsInteractor = analyticsInteractor,
        ) as T
    }
}
