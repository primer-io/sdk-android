package io.primer.android.presentation.mock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.mock.FinaliseMockedFlowInteractor
import io.primer.android.presentation.base.BaseViewModel
import kotlinx.coroutines.launch

internal class PaymentMethodMockViewModel(
    private val finaliseMockedFlowInteractor: FinaliseMockedFlowInteractor,
    private val analyticsInteractor: AnalyticsInteractor
) : BaseViewModel(analyticsInteractor) {

    private val _finalizeMocked: MutableLiveData<Unit> = MutableLiveData()
    val finalizeMocked: LiveData<Unit> = _finalizeMocked

    fun finaliseMockedFlow() =
        viewModelScope.launch {
            finaliseMockedFlowInteractor(None()).collect {
                _finalizeMocked.postValue(Unit)
            }
        }
}
