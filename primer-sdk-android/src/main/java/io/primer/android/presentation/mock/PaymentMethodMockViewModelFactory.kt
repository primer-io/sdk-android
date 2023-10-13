package io.primer.android.presentation.mock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.domain.mock.FinaliseMockedFlowInteractor

internal class PaymentMethodMockViewModelFactory(
    private val finaliseMockedFlowInteractor: FinaliseMockedFlowInteractor,
    private val analyticsInteractor: AnalyticsInteractor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return PaymentMethodMockViewModel(
            finaliseMockedFlowInteractor,
            analyticsInteractor
        ) as T
    }
}
