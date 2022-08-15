package io.primer.android.components.ui.widgets

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.ui.CardNumberFormatter
import io.primer.android.ui.CardNetwork
import io.primer.android.ui.TextInputMask

internal interface PrimerInputElementCardNumberListener {
    fun inputElementCardChanged(cardNumber: String)
}

class PrimerCardNumberEditText(context: Context, attrs: AttributeSet? = null) :
    PrimerEditText(context, attrs) {

    private var cardNetwork: CardNetwork.Type = CardNetwork.Type.UNKNOWN
    private var typeListener: PrimerInputElementCardNumberListener? = null

    init {
        attachTextFormatter(TextInputMask.CardNumber())
        keyListener = DigitsKeyListener.getInstance(ALLOWED_CHARS)
        setRawInputType(InputType.TYPE_CLASS_NUMBER)
    }

    override fun getType() = PrimerInputElementType.CARD_NUMBER

    override fun isValid() =
        CardNumberFormatter.fromString(super.getSanitizedText().toString()).isValid()

    override fun onTextChanged(s: Editable?) {
        super.onTextChanged(s)
        invokeCardTypeListener()
        invokeCardNumberListener()
    }

    internal fun setCvvListener(typeListener: PrimerInputElementCardNumberListener) {
        this.typeListener = typeListener
    }

    private fun invokeCardTypeListener() {
        val cardType = getCardType(super.getSanitizedText().toString())
        if (this.cardNetwork != cardType) {
            this.cardNetwork = cardType
            listener?.inputElementDidDetectCardType(cardType)
        }
    }

    private fun invokeCardNumberListener() {
        typeListener?.inputElementCardChanged(super.getSanitizedText().toString())
    }

    private fun getCardType(cardNumber: String) = CardNumberFormatter.fromString(cardNumber)
        .getCardType()

    private companion object {
        const val ALLOWED_CHARS = "0123456789 "
    }
}
