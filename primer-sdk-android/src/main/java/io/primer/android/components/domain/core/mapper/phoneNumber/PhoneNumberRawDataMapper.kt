package io.primer.android.components.domain.core.mapper.phoneNumber

import io.primer.android.components.data.payments.paymentMethods.nativeUi.async.redirect.exception.AsyncIllegalValueKey
import io.primer.android.components.domain.core.mapper.PrimerPaymentMethodRawDataMapper
import io.primer.android.components.domain.core.models.phoneNumber.PrimerPhoneNumberData
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.async.phone.PhonePaymentInstrumentParams

internal class PhoneNumberRawDataMapper(
    private val deeplinkRepository: AsyncPaymentMethodDeeplinkRepository,
    private val config: PaymentMethodConfigDataResponse,
    private val settings: PrimerSettings
) : PrimerPaymentMethodRawDataMapper<PrimerPhoneNumberData> {
    override fun getInstrumentParams(
        rawData: PrimerPhoneNumberData
    ): BasePaymentInstrumentParams {
        return PhonePaymentInstrumentParams(
            config.type,
            requireNotNullCheck(config.id, AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID),
            settings.locale.toLanguageTag(),
            deeplinkRepository.getDeeplinkUrl(),
            rawData.phoneNumber
        )
    }
}
