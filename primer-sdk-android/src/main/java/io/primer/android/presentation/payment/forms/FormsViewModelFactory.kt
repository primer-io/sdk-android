package io.primer.android.presentation.payment.forms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor
import io.primer.android.domain.payments.forms.FormValidationInteractor
import io.primer.android.domain.payments.forms.FormsInteractor

internal class FormsViewModelFactory(
    private val formsInteractor: FormsInteractor,
    private val formValidationInteractor: FormValidationInteractor,
    private val paymentMethodInteractor: AsyncPaymentMethodInteractor,
    private val analyticsInteractor: AnalyticsInteractor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return FormsViewModel(
            formsInteractor,
            formValidationInteractor,
            paymentMethodInteractor,
            analyticsInteractor
        ) as T
    }
}
