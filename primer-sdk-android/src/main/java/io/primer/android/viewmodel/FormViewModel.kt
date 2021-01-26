package io.primer.android.viewmodel

import android.text.InputType
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.ui.fragments.FormSummaryFragment
import java.util.*
import kotlin.collections.HashMap

data class FormTitleState(
  val titleId: Int,
  val descriptionId: Int? = null,
)

data class FormField(
  val name: String,
  val labelId: Int,
  val inputType: Int,
  val required: Boolean = true,
  val autoFocus: Boolean = false,
  val minLength: Int = 0,
  val maxLength: Int = Int.MAX_VALUE,
  val minWordCount: Int = 0,
)

data class ButtonState(
  val labelId: Int,
  val loading: Boolean = false,
)

data class InteractiveSummaryItem(
  val name: String,
  val iconId: Int,
  val label: String,
)

data class TextSummaryItem(
  val content: String,
  val styleId: Int = R.style.Primer_Text_Small_Muted
)

data class FormSummaryState(
  val items: List<InteractiveSummaryItem>,
  val text: List<TextSummaryItem>
)

data class FormState(
  val submitted: Boolean = false,
  val isValid: Boolean = false,
)

internal class FormViewModel: ViewModel() {
  private class Validator(private val field: FormField) {
    fun validate(value: String): SyncValidationError? {
      val normalized = (value ?: "").trim()

      if (field.required && normalized.isEmpty()) {
        return required()
      }

      if (normalized.length < field.minLength) {
        return invalid()
      }

      if (normalized.length > field.maxLength) {
        return invalid()
      }

      val numWords = normalized.split(Regex("\\s+")).size

      if (numWords < field.minWordCount) {
        return invalid()
      }

      return null
    }

    private fun required(): SyncValidationError {
      return error(R.string.form_error_required)
    }

    private fun invalid(): SyncValidationError {
      return error(R.string.form_error_invalid)
    }

    private fun error(errorId: Int): SyncValidationError {
      return SyncValidationError(name = field.name, errorId = errorId, fieldId = field.labelId)
    }
  }

  private val log = Logger("form-view-model")

  interface ButtonPressListener {
    fun onButtonPressed()
  }

  interface SummaryItemPressListener {
    fun onSummaryItemPressed(name: String)
  }

  private var buttonPressListener: ButtonPressListener? = null
  private var summaryItemPressListener: SummaryItemPressListener? = null

  private var initialValues: Map<String, String> = Collections.emptyMap()

  val title: MutableLiveData<FormTitleState?> = MutableLiveData()

  val fields: MutableLiveData<List<FormField>> = MutableLiveData(Collections.emptyList())

  val summary: MutableLiveData<FormSummaryState?> = MutableLiveData()

  var fieldValues: MutableMap<String, String> = HashMap()

  val errors: MutableLiveData<MutableMap<String, SyncValidationError?>> = MutableLiveData(Collections.emptyMap())

  val button: MutableLiveData<ButtonState?> = MutableLiveData()

  val meta: MutableLiveData<FormState> = MutableLiveData(FormState())

  val errorId: MutableLiveData<Int?> = MutableLiveData()

  fun reset() {
    title.value = null
    button.value = null
    summary.value = null
    errorId.value = null
    buttonPressListener = null
    meta.value = FormState()
    fields.value = Collections.emptyList()
    errors.value = Collections.emptyMap()
    fieldValues.clear()
  }

  fun setFieldValue(key: String, value: String) {
    fieldValues[key] = value
    validate(key)
  }

  fun setOnButtonPressListener(l: ButtonPressListener) {
    buttonPressListener = l
  }

  fun setOnSummaryItemPressListener(l: SummaryItemPressListener) {
    summaryItemPressListener = l
  }

  fun onButtonPress() {
    meta.value?.isValid?.let { valid ->
      if (valid) {
        buttonPressListener?.onButtonPressed()
      }
    }
  }

  fun onSummaryItemPressed(name: String) {
    summaryItemPressListener?.onSummaryItemPressed(name)
  }

  fun setInitialValues(values: Map<String, String>) {
    initialValues = values
    fieldValues = HashMap(initialValues)
  }

  fun getValue(name: String): String {
    return fieldValues[name] ?: ""
  }

  private fun validate(name: String) {
    val error = getField(name)?.let { Validator(it).validate(fieldValues[name] ?: "") }
    val nextErrors = errors.value?.let { HashMap(it).apply { set(name, error) } } ?: Collections.emptyMap()

    errors.value = nextErrors

    meta.value = FormState(
      isValid = nextErrors.values.filterNotNull().isEmpty(),
      submitted = meta.value?.submitted ?: false,
    )
  }

  private fun getField(name: String): FormField? {
    return fields.value?.find { it.name == name }
  }
}