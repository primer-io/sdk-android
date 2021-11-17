package io.primer.android.presentation.payment.async

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor
import io.primer.android.domain.payments.async.models.AsyncMethodParams
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class AsyncPaymentMethodViewModel(
    private val paymentMethodInteractor: AsyncPaymentMethodInteractor
) : ViewModel() {

    private val _statusUrlLiveData = MutableLiveData<Unit>()
    val statusUrlLiveData: LiveData<Unit> = _statusUrlLiveData

    private val _statusUrlErrorData = MutableLiveData<Unit>()
    val statusUrlErrorData: LiveData<Unit> = _statusUrlErrorData

    fun getStatus(statusUrl: String) {
        viewModelScope.launch {
            paymentMethodInteractor(AsyncMethodParams(statusUrl))
                .catch {
                    _statusUrlErrorData.postValue(Unit)
                }.collect {
                    _statusUrlLiveData.postValue(Unit)
                }
        }
    }
}
