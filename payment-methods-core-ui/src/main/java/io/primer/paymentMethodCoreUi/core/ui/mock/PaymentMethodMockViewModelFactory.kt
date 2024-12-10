package io.primer.paymentMethodCoreUi.core.ui.mock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.configuration.mock.domain.FinaliseMockedFlowInteractor
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler

class PaymentMethodMockViewModelFactory(
    private val finaliseMockedFlowInteractor: FinaliseMockedFlowInteractor,
    private val paymentResumeHandler: PaymentResumeHandler,
    private val errorHandler: CheckoutErrorHandler,
    private val successHandler: CheckoutSuccessHandler
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return PaymentMethodMockViewModel(
            finaliseMockedFlowInteractor = finaliseMockedFlowInteractor,
            paymentResumeHandler = paymentResumeHandler,
            errorHandler = errorHandler,
            successHandler = successHandler
        ) as T
    }
}
