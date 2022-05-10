package io.primer.android.presentation.payment.async

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor
import io.primer.android.domain.payments.async.models.AsyncMethodParams
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

internal class AsyncPaymentMethodViewModel(
    private val paymentMethodInteractor: AsyncPaymentMethodInteractor,
    analyticsInteractor: AnalyticsInteractor
) : BaseViewModel(analyticsInteractor) {

    private val _statusUrlLiveData = MutableLiveData<Unit>()
    val statusUrlLiveData: LiveData<Unit> = _statusUrlLiveData

    private val _statusUrlErrorData = MutableLiveData<Unit>()
    val statusUrlErrorData: LiveData<Unit> = _statusUrlErrorData

    fun getStatus(statusUrl: String, paymentMethodType: PaymentMethodType) {
        viewModelScope.launch {
            paymentMethodInteractor(AsyncMethodParams(statusUrl, paymentMethodType))
                .catch {
                    _statusUrlErrorData.postValue(Unit)
                }.collect {
                    _statusUrlLiveData.postValue(Unit)
                }
        }
    }
}
