package io.primer.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.ui.ButtonState
import io.primer.android.ui.FormErrorState
import io.primer.android.ui.FormField
import io.primer.android.ui.FormProgressState
import io.primer.android.ui.FormSummaryState
import io.primer.android.ui.FormTitleState
import io.primer.android.ui.FormViewState

internal class FormViewModel : ViewModel() {

    private val values: MutableMap<String, String> = HashMap()
    private val initialValues: MutableMap<String, String> = HashMap()

    val title: MutableLiveData<FormTitleState> = MutableLiveData()
    val fields: MutableLiveData<List<FormField>> = MutableLiveData(emptyList())
    val submitted: MutableLiveData<Boolean> = MutableLiveData(false)
    val isValid: MutableLiveData<Boolean> = MutableLiveData(true)
    val button: MutableLiveData<ButtonState?> = MutableLiveData()
    val summary: MutableLiveData<FormSummaryState?> = MutableLiveData()
    val error: MutableLiveData<FormErrorState?> = MutableLiveData()
    val progress: MutableLiveData<FormProgressState?> = MutableLiveData()
    val validationErrors: MutableLiveData<MutableMap<String, SyncValidationError?>> =
        MutableLiveData(HashMap())

    fun setState(state: FormViewState) {
        title.postValue(state.title)
        fields.postValue(state.fields)
        button.postValue(state.button)
        summary.postValue(state.summary)
        progress.postValue(state.progress)
        error.postValue(null)

        state.initialValues?.let {
            setInitialValues(it)
        }
    }

    private fun setInitialValues(map: Map<String, String>) {
        initialValues.clear()
        values.clear()

        map.forEach {
            initialValues[it.key] = it.value
            setValue(it.key, it.value)
        }
    }

    fun setValue(key: String, value: String) {
        values[key] = value
        validate(key)
    }

    fun getValue(key: String): String {
        return values[key] ?: ""
    }

    private fun validate(key: String) {
        fields.value?.find { it.name == key }?.let {
            val validator = Validator(it)
            val value = getValue(key)
            validator.validate(value).let { error ->
                validationErrors.value = HashMap(validationErrors.value ?: HashMap()).apply {
                    set(key, error)
                }
            }
        }

        val valid = validationErrors.value?.filter { it.value != null }?.isEmpty() ?: true

        isValid.value = valid
    }

    fun setLoading(loading: Boolean) {
        button.value = button.value?.copy(loading = loading)
        error.value = null
    }
}
