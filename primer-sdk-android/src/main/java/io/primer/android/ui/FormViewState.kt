package io.primer.android.ui

import io.primer.android.R
import io.primer.android.model.dto.SyncValidationError
import java.util.*

internal class Validator(private val field: FormField) {
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
  val placement: Placement = Placement.CENTER,
  val loading: Boolean = false,
) {
  enum class Placement {
    LEFT,
    CENTER,
    RIGHT,
  }
}

data class InteractiveSummaryItem(
  val name: String,
  val iconId: Int,
  val getLabel: (() -> String),
)

data class TextSummaryItem(
  val content: String,
  val styleId: Int = R.style.Primer_Text_Small_Muted
)

data class FormSummaryState(
  val items: List<InteractiveSummaryItem>,
  val text: List<TextSummaryItem>
)

data class FormErrorState(
  val labelId: Int
)

open class FormViewState(
  val title: FormTitleState? = null,
  val fields: List<FormField> = Collections.emptyList(),
  val button: ButtonState? = null,
  val summary: FormSummaryState? = null,
  val initialValues: Map<String, String>? = null,
)