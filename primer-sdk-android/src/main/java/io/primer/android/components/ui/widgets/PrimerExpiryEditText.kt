package io.primer.android.components.ui.widgets

import android.content.Context
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import io.primer.android.model.dto.PrimerInputFieldType
import io.primer.android.ui.ExpiryDateFormatter
import io.primer.android.ui.TextInputMask

internal class PrimerExpiryEditText(context: Context, attrs: AttributeSet? = null) :
    PrimerEditText(context, attrs) {

    init {
        attachTextFormatter(TextInputMask.ExpiryDate())
        keyListener = DigitsKeyListener.getInstance(ALLOWED_CHARS)
        setRawInputType(InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE)
    }

    override fun getType() = PrimerInputFieldType.EXPIRY_DATE

    override fun isValid() = ExpiryDateFormatter.fromString(super.getSanitizedText().toString())
        .isValid()

    private companion object {
        const val ALLOWED_CHARS = "0123456789/ "
    }
}
