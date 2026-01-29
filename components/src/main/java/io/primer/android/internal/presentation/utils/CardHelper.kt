package io.primer.android.internal.presentation.utils

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

internal val CARD_FIELDS = setOf(
    PrimerInputElementType.CARD_NUMBER,
    PrimerInputElementType.CVV,
    PrimerInputElementType.EXPIRY_DATE,
    PrimerInputElementType.CARDHOLDER_NAME,
)

internal val BILLING_FIELDS = setOf(
    PrimerInputElementType.POSTAL_CODE,
    PrimerInputElementType.COUNTRY_CODE,
    PrimerInputElementType.CITY,
    PrimerInputElementType.STATE,
    PrimerInputElementType.ADDRESS_LINE_1,
    PrimerInputElementType.ADDRESS_LINE_2,
    PrimerInputElementType.FIRST_NAME,
    PrimerInputElementType.LAST_NAME,
)

internal fun Map<PrimerInputElementType, String>.toPrimerCardData(
    cardNetwork: PrimerCardNetwork? = null,
): PrimerCardData = PrimerCardData(
    cardNumber = get(PrimerInputElementType.CARD_NUMBER) ?: "",
    expiryDate = get(PrimerInputElementType.EXPIRY_DATE)?.formatExpiryDate() ?: "",
    cvv = get(PrimerInputElementType.CVV) ?: "",
    cardHolderName = get(PrimerInputElementType.CARDHOLDER_NAME)?.takeIf { it.isNotEmpty() },
    cardNetwork = cardNetwork?.network,
)

@Suppress("MagicNumber")
internal fun String.formatExpiryDate(): String = when {
    isEmpty() || length <= 2 -> this
    length == 4 && !contains("/") -> "${take(2)}/20${drop(2)}"
    contains("/") -> split("/").let { parts ->
        if (parts.size == 2 && parts[0].length == 2 && parts[1].length == 2) {
            "${parts[0]}/20${parts[1]}"
        } else {
            this
        }
    }
    length == 6 -> "${take(2)}/${drop(2)}"
    else -> this
}
