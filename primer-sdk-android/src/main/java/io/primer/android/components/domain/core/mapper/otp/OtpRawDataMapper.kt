package io.primer.android.components.domain.core.mapper.otp

import io.primer.android.components.data.payments.paymentMethods.nativeUi.async.redirect.exception.AsyncIllegalValueKey
import io.primer.android.components.domain.core.mapper.PrimerPaymentMethodRawDataMapper
import io.primer.android.components.domain.core.models.otp.PrimerOtpCodeData
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.async.blik.AdyenBlikPaymentInstrumentParams

internal class OtpRawDataMapper(
    private val deeplinkRepository: AsyncPaymentMethodDeeplinkRepository,
    private val config: PaymentMethodConfigDataResponse,
    private val settings: PrimerSettings,
) :
    PrimerPaymentMethodRawDataMapper<PrimerOtpCodeData> {
    override fun getInstrumentParams(
        rawData: PrimerOtpCodeData
    ): BasePaymentInstrumentParams {
        return AdyenBlikPaymentInstrumentParams(
            config.type,
            requireNotNullCheck(config.id, AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID),
            settings.locale.toLanguageTag(),
            deeplinkRepository.getDeeplinkUrl(),
            rawData.otpCode
        )
    }
}
