package io.primer.android.presentation.payment.forms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.domain.payments.forms.FormsInteractor
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.domain.payments.forms.models.FormInputParams
import io.primer.android.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class FormsViewModel(
    private val formsInteractor: FormsInteractor,
    analyticsInteractor: AnalyticsInteractor
) : BaseViewModel(analyticsInteractor) {

    private val inputStates: MutableMap<String, InputState> = mutableMapOf()

    private val _formLiveData = MutableLiveData<Form>()
    val formLiveData: LiveData<Form> = _formLiveData

    fun getForms(type: String) = viewModelScope.launch {
        formsInteractor(FormInputParams(type))
            .onEach { it.inputs?.forEach { input -> inputStates[input.id] = InputState(null, false) } }
            .collect {
                _formLiveData.postValue(it)
            }
    }

    private data class InputState(val input: CharSequence?, val validated: Boolean)
}
