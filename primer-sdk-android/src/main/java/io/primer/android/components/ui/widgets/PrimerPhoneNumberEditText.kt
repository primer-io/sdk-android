package io.primer.android.components.ui.widgets

import android.content.Context
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import io.primer.android.model.dto.PrimerInputFieldType

internal class PrimerPhoneNumberEditText(context: Context, attrs: AttributeSet? = null) :
    PrimerEditText(context, attrs) {

    init {
        keyListener = DigitsKeyListener.getInstance(false, false)
    }

    override fun getType() = PrimerInputFieldType.PHONE_NUMBER

    override fun isValid() = super.getSanitizedText().isNullOrBlank().not()
}
