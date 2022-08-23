package io.primer.android.presentation.payment.forms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.data.payments.forms.models.FormType
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor
import io.primer.android.domain.payments.async.models.AsyncMethodParams
import io.primer.android.domain.payments.forms.FormValidationInteractor
import io.primer.android.domain.payments.forms.FormsInteractor
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.domain.payments.forms.models.FormInputParams
import io.primer.android.domain.payments.forms.models.FormValidationParam
import io.primer.android.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class FormsViewModel(
    private val formsInteractor: FormsInteractor,
    private val formValidationInteractor: FormValidationInteractor,
    private val paymentMethodInteractor: AsyncPaymentMethodInteractor,
    private val analyticsInteractor: AnalyticsInteractor
) : BaseViewModel(analyticsInteractor) {

    private val inputStates: MutableMap<String, InputState> = mutableMapOf()

    private val _formLiveData = MutableLiveData<Form>()
    val formLiveData: LiveData<Form> = _formLiveData

    private val _validationLiveData = MutableLiveData<Boolean>()
    val validationLiveData: LiveData<Boolean> = _validationLiveData

    private val _statusUrlLiveData = MutableLiveData<Unit>()
    val statusUrlLiveData: LiveData<Unit> = _statusUrlLiveData

    private val _statusUrlErrorData = MutableLiveData<Unit>()
    val statusUrlErrorData: LiveData<Unit> = _statusUrlErrorData

    fun getForms(type: String) = viewModelScope.launch {
        formsInteractor(FormInputParams(type))
            .onEach { it.inputs?.forEach { inputStates[it.id] = InputState(null, false) } }
            .collect {
                _validationLiveData.postValue(inputStates.values.all { it.validated })
                _formLiveData.postValue(it)
            }
    }

    fun onInputChanged(
        id: String,
        formType: FormType,
        inputForValidation: CharSequence?,
        data: CharSequence?,
        regex: Regex?
    ) = viewModelScope.launch {
        formValidationInteractor.execute(FormValidationParam(inputForValidation, formType, regex))
            .collect { validated ->
                inputStates[id] = InputState(data, validated)
                _validationLiveData.postValue(inputStates.values.all { it.validated })
            }
    }

    fun collectData() = inputStates.map { it.key to it.value.input }

    fun getStatus(statusUrl: String, paymentMethodType: String) {
        viewModelScope.launch {
            paymentMethodInteractor(AsyncMethodParams(statusUrl, paymentMethodType))
                .catch {
                    _statusUrlErrorData.postValue(Unit)
                }.collect {
                    _statusUrlLiveData.postValue(Unit)
                }
        }
    }

    private data class InputState(val input: CharSequence?, val validated: Boolean)
}
