package io.primer.android.components.ui.widgets

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.ui.ExpiryDateFormatter
import io.primer.android.ui.TextInputMask

@Deprecated(
    "Card components will no longer receive ongoing maintenance and will be removed in future."
)
class PrimerExpiryEditText(context: Context, attrs: AttributeSet? = null) :
    PrimerEditText(context, attrs) {

    init {
        attachTextFormatter(TextInputMask.ExpiryDate())
        keyListener = DigitsKeyListener.getInstance(ALLOWED_CHARS)
        setRawInputType(InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE)
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(EXPIRY_DATE_MAX_LENGTH))
    }

    override fun getType() = PrimerInputElementType.EXPIRY_DATE

    override fun isValid() = ExpiryDateFormatter.fromString(super.getSanitizedText().toString())
        .isValid()

    private companion object {
        const val EXPIRY_DATE_MAX_LENGTH = 5
        const val ALLOWED_CHARS = "0123456789/ "
    }
}
