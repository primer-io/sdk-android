package io.primer.paymentMethodCoreUi.core.ui.mock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.configuration.mock.domain.FinaliseMockedFlowInteractor
import io.primer.android.core.domain.None
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import kotlinx.coroutines.launch

internal class PaymentMethodMockViewModel(
    private val finaliseMockedFlowInteractor: FinaliseMockedFlowInteractor,
    private val paymentResumeHandler: PaymentResumeHandler,
    private val errorHandler: CheckoutErrorHandler,
    private val successHandler: CheckoutSuccessHandler
) : ViewModel() {

    private val _finalizeMocked: MutableLiveData<Unit> = MutableLiveData()
    val finalizeMocked: LiveData<Unit> = _finalizeMocked

    fun finaliseMockedFlow() = viewModelScope.launch {
        finaliseMockedFlowInteractor(None).onSuccess {
            _finalizeMocked.postValue(Unit)
        }
    }
}
