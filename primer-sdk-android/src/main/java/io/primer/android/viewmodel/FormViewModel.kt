package io.primer.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.primer.android.logging.Logger
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.ui.*
import java.util.*
import kotlin.collections.HashMap


internal class FormViewModel : ViewModel() {

  private val log = Logger("form-view-model")

  val title: MutableLiveData<FormTitleState> = MutableLiveData()
  val fields: MutableLiveData<List<FormField>> = MutableLiveData(Collections.emptyList())
  val submitted: MutableLiveData<Boolean> = MutableLiveData(false)
  val isValid: MutableLiveData<Boolean> = MutableLiveData(true)
  val button: MutableLiveData<ButtonState?> = MutableLiveData()
  val summary: MutableLiveData<FormSummaryState?> = MutableLiveData()
  val error: MutableLiveData<FormErrorState?> = MutableLiveData()
  val validationErrors: MutableLiveData<Map<String, SyncValidationError?>> = MutableLiveData(HashMap())
  val values: MutableMap<String, String> = HashMap()
  val initialValues: MutableMap<String, String> = HashMap()

  fun setState(state : FormViewState) {
    title.value = state.title
    fields.value = state.fields
    button.value = state.button
    summary.value = state.summary

    state.initialValues?.let {
      setInitialValues(it)
    }
  }

  fun setInitialValues(map: Map<String, String>) {
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

  fun getError(name: String) : SyncValidationError? {
    return validationErrors.value?.get(name)
  }

  fun validate(key: String) {
    fields.value?.find { it.name == key }?.let {
      val validator = Validator(it)
      val value = getValue(key)
      validator.validate(value).let { error ->
        validationErrors.value = HashMap(validationErrors.value).apply {
          set(key, error)
        }
      }
    }

    isValid.value = validationErrors.value?.filter { it.value != null }?.isEmpty() ?: true
  }

  fun setSubmitted(value: Boolean) {
    this.submitted.value = value
  }
}