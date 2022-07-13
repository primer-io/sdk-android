package io.primer.android.components.domain.core.models.card

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.payment.card.CreditCard

internal data class CardInputData(
    val number: String,
    val expiryDate: String,
    val cvv: String,
    val holderName: String? = null,
    val postalCode: String? = null
) : PrimerHeadlessUniversalCheckoutInputData {

    internal fun setTokenizableValues(creditCard: CreditCard) = creditCard.apply {
        setTokenizableField(PrimerInputElementType.CARD_NUMBER, number)
        setTokenizableField(PrimerInputElementType.EXPIRY_DATE, expiryDate)
        setTokenizableField(PrimerInputElementType.CVV, cvv)
        holderName?.apply {
            setTokenizableField(PrimerInputElementType.CARDHOLDER_NAME, holderName)
        }
        postalCode?.apply {
            setTokenizableField(PrimerInputElementType.POSTAL_CODE, postalCode)
        }
    }
}
