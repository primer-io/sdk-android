package io.primer.android.components.ui.widgets

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import io.primer.android.model.dto.PrimerInputFieldType
import io.primer.android.ui.CardNumberFormatter

class PrimerCvvEditText(context: Context, attrs: AttributeSet? = null) :
    PrimerEditText(context, attrs) {

    init {
        setRawInputType(InputType.TYPE_CLASS_NUMBER)
        keyListener = DigitsKeyListener.getInstance(false, false)
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(CVV_MAX_LENGTH))
    }

    private var cardNumber: String? = null

    override fun getType() = PrimerInputFieldType.CVV

    override fun isValid() = cardNumber.isNullOrBlank().not() && CardNumberFormatter.fromString(
        cardNumber.orEmpty()
    ).getCvvLength() == getSanitizedText()?.length && getSanitizedText().isNullOrBlank()
        .not()

    internal fun onCardNumberChanged(cardNumber: String) {
        val lastValid = isValid()
        this.cardNumber = cardNumber
        if (lastValid != isValid()) listener?.inputElementValueIsValid(this, isValid())
    }

    private companion object {
        const val CVV_MAX_LENGTH = 4
    }
}
