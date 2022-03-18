package io.primer.android.components.domain.core.models.card

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import io.primer.android.model.dto.PrimerInputFieldType
import io.primer.android.payment.card.CreditCard

internal data class CardInputData(
    val number: String,
    val expiryDate: String,
    val cvv: String,
    val holderName: String? = null,
    val postalCode: String? = null
) : PrimerHeadlessUniversalCheckoutInputData {

    internal fun setTokenizableValues(creditCard: CreditCard) = creditCard.apply {
        setTokenizableField(PrimerInputFieldType.CARD_NUMBER, number)
        setTokenizableField(PrimerInputFieldType.EXPIRY_DATE, expiryDate)
        setTokenizableField(PrimerInputFieldType.CVV, cvv)
        holderName?.apply {
            setTokenizableField(PrimerInputFieldType.CARDHOLDER_NAME, holderName)
        }
        postalCode?.apply {
            setTokenizableField(PrimerInputFieldType.POSTAL_CODE, postalCode)
        }
    }
}
