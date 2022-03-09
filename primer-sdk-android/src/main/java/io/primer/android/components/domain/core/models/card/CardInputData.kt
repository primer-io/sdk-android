package io.primer.android.components.domain.core.models.card

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import io.primer.android.payment.card.CARD_CVV_FIELD_NAME
import io.primer.android.payment.card.CARD_EXPIRY_FIELD_NAME
import io.primer.android.payment.card.CARD_NAME_FILED_NAME
import io.primer.android.payment.card.CARD_NUMBER_FIELD_NAME
import io.primer.android.payment.card.CreditCard

internal data class CardInputData(
    val number: String,
    val expiryDate: String,
    val cvv: String,
    val holderName: String? = null,
    val postalCode: String? = null
) : PrimerHeadlessUniversalCheckoutInputData {

    internal fun setTokenizableValues(creditCard: CreditCard) = creditCard.apply {
        setTokenizableValue(CARD_NUMBER_FIELD_NAME, number)
        setTokenizableValue(CARD_EXPIRY_FIELD_NAME, expiryDate)
        setTokenizableValue(CARD_CVV_FIELD_NAME, cvv)
        holderName?.apply {
            setTokenizableValue(CARD_NAME_FILED_NAME, holderName)
        }
        postalCode?.apply {
            setTokenizableValue(CARD_NAME_FILED_NAME, postalCode)
        }
    }
}
