package io.primer.android.components.domain.core.mapper.bancontact

import io.primer.android.components.data.payments.paymentMethods.nativeUi.async.redirect.exception.AsyncIllegalValueKey
import io.primer.android.components.domain.core.mapper.PrimerPaymentMethodRawDataMapper
import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardData
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.bancontactCard.AdyenBancontactCardPaymentInstrumentParams
import io.primer.android.utils.removeSpaces

internal class BancontactRawCardDataMapper(
    private val asyncPaymentMethodDeeplinkRepository: AsyncPaymentMethodDeeplinkRepository,
    private val config: PaymentMethodConfigDataResponse,
    private val settings: PrimerSettings
) : PrimerPaymentMethodRawDataMapper<PrimerBancontactCardData> {
    override fun getInstrumentParams(
        rawData: PrimerBancontactCardData
    ): BasePaymentInstrumentParams {
        return AdyenBancontactCardPaymentInstrumentParams(
            config.type,
            requireNotNullCheck(config.id, AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID),
            settings.locale.toLanguageTag(),
            asyncPaymentMethodDeeplinkRepository.getDeeplinkUrl(),
            rawData.cardNumber.removeSpaces(),
            rawData.expiryDate.split("/").first().padStart(
                EXPIRATION_MONTH_PAD_START_LENGTH,
                EXPIRATION_MONTH_PAD_START_CHAR
            ),
            rawData.expiryDate.split("/")[1],
            rawData.cardHolderName,
            System.getProperty("http.agent").orEmpty()
        )
    }

    private companion object {
        private const val EXPIRATION_MONTH_PAD_START_LENGTH = 2
        private const val EXPIRATION_MONTH_PAD_START_CHAR = '0'
    }
}
