package io.primer.android.components.ui.widgets

import android.content.Context
import android.text.method.QwertyKeyListener
import android.text.method.TextKeyListener
import android.util.AttributeSet
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

internal class PrimerStateEditText(context: Context, attrs: AttributeSet? = null) :
    PrimerEditText(context, attrs) {

    init {
        keyListener = QwertyKeyListener.getInstance(true, TextKeyListener.Capitalize.WORDS)
    }

    override fun getType() = PrimerInputElementType.STATE

    override fun isValid() = super.getSanitizedText().isNullOrBlank().not()
}
