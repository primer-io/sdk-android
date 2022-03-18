package io.primer.android.components.ui.widgets

import android.content.Context
import android.text.method.QwertyKeyListener
import android.text.method.TextKeyListener
import android.util.AttributeSet
import io.primer.android.model.dto.PrimerInputFieldType

class PrimerFirstNameEditText(context: Context, attrs: AttributeSet? = null) :
    PrimerEditText(context, attrs) {

    init {
        keyListener = QwertyKeyListener.getInstance(true, TextKeyListener.Capitalize.WORDS)
    }

    override fun getType() = PrimerInputFieldType.FIRST_NAME

    override fun isValid() = super.getSanitizedText().isNullOrBlank().not()
}
