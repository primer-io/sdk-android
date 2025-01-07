package io.primer.paymentMethodCoreUi.core.ui.mock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.configuration.mock.domain.FinaliseMockedFlowInteractor
import io.primer.android.core.domain.None
import kotlinx.coroutines.launch

internal class PaymentMethodMockViewModel(
    private val finaliseMockedFlowInteractor: FinaliseMockedFlowInteractor,
) : ViewModel() {
    private val _finalizeMocked: MutableLiveData<Unit> = MutableLiveData()
    val finalizeMocked: LiveData<Unit> = _finalizeMocked

    fun finaliseMockedFlow() =
        viewModelScope.launch {
            finaliseMockedFlowInteractor(None).onSuccess {
                _finalizeMocked.postValue(Unit)
            }
        }
}
