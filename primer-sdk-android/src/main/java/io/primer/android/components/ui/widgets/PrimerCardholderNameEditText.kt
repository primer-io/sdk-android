package io.primer.android.components.ui.widgets

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

@Deprecated(
    "Card components will no longer receive ongoing maintenance and will be removed in future."
)
class PrimerCardholderNameEditText(context: Context, attrs: AttributeSet? = null) :
    PrimerEditText(context, attrs) {

    override fun getText(): Editable? {
        val text = super.getText()
        return if (isCalledFromSuperMethod()) {
            text
        } else { SpannableStringBuilder(getSanitizedText()) }
    }

    override fun getType() = PrimerInputElementType.CARDHOLDER_NAME

    override fun isValid() = getSanitizedText().isNullOrBlank().not()
}
