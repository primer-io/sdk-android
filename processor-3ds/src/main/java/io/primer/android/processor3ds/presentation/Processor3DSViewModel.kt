package io.primer.android.processor3ds.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

internal class Processor3DSViewModel(
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
) : ViewModel() {
    private val _statusUrlLiveData = MutableLiveData<String>()
    val statusUrlLiveData: LiveData<String> = _statusUrlLiveData

    private val _statusUrlErrorData = MutableLiveData<Throwable>()
    val statusUrlErrorData: LiveData<Throwable> = _statusUrlErrorData

    fun getStatus(
        statusUrl: String,
        paymentMethodType: String,
    ) {
        viewModelScope.launch {
            pollingInteractor(
                AsyncStatusParams(
                    url = statusUrl,
                    paymentMethodType = paymentMethodType,
                ),
            ).catch { throwable ->
                _statusUrlErrorData.postValue(throwable)
            }.collect { status ->
                _statusUrlLiveData.postValue(status.resumeToken)
            }
        }
    }

    fun addAnalyticsEvent(params: BaseAnalyticsParams) =
        viewModelScope.launch {
            analyticsInteractor(params)
        }
}
