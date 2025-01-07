package io.primer.paymentMethodCoreUi.core.ui.mock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.configuration.mock.domain.FinaliseMockedFlowInteractor

class PaymentMethodMockViewModelFactory(
    private val finaliseMockedFlowInteractor: FinaliseMockedFlowInteractor,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        return PaymentMethodMockViewModel(
            finaliseMockedFlowInteractor = finaliseMockedFlowInteractor,
        ) as T
    }
}
