package io.primer.android.components.domain.core.mapper.card

import io.primer.android.components.domain.core.mapper.PrimerPaymentMethodRawDataMapper
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.card.CardPaymentInstrumentParams
import io.primer.android.utils.removeSpaces

internal class CardRawDataMapper(private val config: PaymentMethodConfigDataResponse) :
    PrimerPaymentMethodRawDataMapper<PrimerCardData> {
    override fun getInstrumentParams(
        rawData: PrimerCardData
    ): BasePaymentInstrumentParams {
        return CardPaymentInstrumentParams(
            config.type,
            rawData.cardNumber.removeSpaces(),
            rawData.expiryDate.split("/").first().padStart(
                EXPIRATION_MONTH_PAD_START_LENGTH,
                EXPIRATION_MONTH_PAD_START_CHAR
            ),
            rawData.expiryDate.split("/")[1],
            rawData.cvv,
            rawData.cardHolderName,
            rawData.cardNetwork
        )
    }

    private companion object {
        private const val EXPIRATION_MONTH_PAD_START_LENGTH = 2
        private const val EXPIRATION_MONTH_PAD_START_CHAR = '0'
    }
}
