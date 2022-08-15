package io.primer.android.components.domain.core.models.card

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.payment.card.CreditCard

data class PrimerRawCardData(
    val cardNumber: String,
    val expirationMonth: String,
    val expirationYear: String,
    val cvv: String,
    val cardHolderName: String? = null,
) : PrimerRawData {

    internal fun setTokenizableValues(creditCard: CreditCard) = creditCard.apply {
        setTokenizableField(PrimerInputElementType.CARD_NUMBER, cardNumber)
        setTokenizableField(
            PrimerInputElementType.EXPIRY_DATE,
            "$expirationMonth/$expirationYear"
        )
        setTokenizableField(PrimerInputElementType.CVV, cvv)
        cardHolderName?.apply {
            setTokenizableField(PrimerInputElementType.FORM_CARDHOLDER_NAME, cardHolderName)
        }
    }
}
