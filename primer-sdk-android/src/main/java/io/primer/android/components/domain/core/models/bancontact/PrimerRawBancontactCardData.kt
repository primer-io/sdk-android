package io.primer.android.components.domain.core.models.bancontact

import io.primer.android.components.domain.core.models.PrimerCardAsyncRawDataTokenizationHelper
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.utils.removeSpaces

data class PrimerRawBancontactCardData(
    val cardNumber: String,
    val expirationMonth: String,
    val expirationYear: String,
    val cardHolderName: String
) : PrimerRawData {

    internal fun setTokenizableValues(
        descriptor: AsyncPaymentMethodDescriptor,
        redirectionUrl: String
    ) = PrimerCardAsyncRawDataTokenizationHelper(redirectionUrl).setTokenizableData(descriptor)
        .apply {
            setTokenizableField(PrimerInputElementType.CARD_NUMBER, cardNumber.removeSpaces())
            setTokenizableField(PrimerInputElementType.EXPIRY_MONTH, expirationMonth)
            setTokenizableField(PrimerInputElementType.EXPIRY_YEAR, expirationYear)
            setTokenizableField(PrimerInputElementType.CARDHOLDER_NAME, cardHolderName)
            appendTokenizableValue(
                "sessionInfo",
                "browserInfo",
                "userAgent",
                System.getProperty("http.agent").orEmpty()
            )
        }
}
