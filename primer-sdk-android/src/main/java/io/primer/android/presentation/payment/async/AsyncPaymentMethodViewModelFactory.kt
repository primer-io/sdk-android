package io.primer.android.presentation.payment.async

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor

internal class AsyncPaymentMethodViewModelFactory(
    private val paymentMethodInteractor: AsyncPaymentMethodInteractor,
    private val analyticsInteractor: AnalyticsInteractor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return AsyncPaymentMethodViewModel(
            paymentMethodInteractor,
            analyticsInteractor
        ) as T
    }
}
