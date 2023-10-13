package io.primer.android.components.domain.core.mapper.retailOutlet

import io.primer.android.components.data.payments.paymentMethods.nativeUi.async.redirect.exception.AsyncIllegalValueKey
import io.primer.android.components.domain.core.mapper.PrimerPaymentMethodRawDataMapper
import io.primer.android.components.domain.core.models.retailOutlet.PrimerRetailerData
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.async.retailOutlet.XenditRetailOutletPaymentInstrumentParams

internal class RetailOutletRawDataMapper(
    private val deeplinkRepository: AsyncPaymentMethodDeeplinkRepository,
    private val config: PaymentMethodConfigDataResponse,
    private val settings: PrimerSettings
) : PrimerPaymentMethodRawDataMapper<PrimerRetailerData> {
    override fun getInstrumentParams(
        rawData: PrimerRetailerData
    ): BasePaymentInstrumentParams {
        return XenditRetailOutletPaymentInstrumentParams(
            config.type,
            requireNotNullCheck(config.id, AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID),
            settings.locale.toLanguageTag(),
            deeplinkRepository.getDeeplinkUrl(),
            rawData.id
        )
    }
}
