package io.primer.android.components.ui.widgets

import android.content.Context
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import io.primer.android.model.dto.PrimerInputFieldType

class PrimerPhoneNumberEditText(context: Context, attrs: AttributeSet? = null) :
    PrimerEditText(context, attrs) {

    init {
        keyListener = DigitsKeyListener.getInstance(false, false)
    }

    override fun getType() = PrimerInputFieldType.STATE

    override fun isValid() = super.getSanitizedText().isNullOrBlank().not()
}
