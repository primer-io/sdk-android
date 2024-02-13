package io.primer.android.components.domain.core.models.card

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.ui.CardNetwork

/**
 * Represents the raw card data required for payment processing.
 *
 * @property cardNumber The card number associated with the payment card.
 * @property expiryDate The expiration date of the payment card in the "MM/YYYY" format.
 * @property cvv The Card Verification Value (CVV) of the payment card.
 * @property cardHolderName The optional name of the cardholder.
 * @property cardNetwork The optional type of card network (e.g., VISA, MASTERCARD) relevant only in
 * case co-badged cards are supported.
 *
 * @see PrimerRawData
 */
data class PrimerCardData(
    val cardNumber: String,
    val expiryDate: String,
    val cvv: String,
    val cardHolderName: String? = null,
    val cardNetwork: CardNetwork.Type? = null
) : PrimerRawData
