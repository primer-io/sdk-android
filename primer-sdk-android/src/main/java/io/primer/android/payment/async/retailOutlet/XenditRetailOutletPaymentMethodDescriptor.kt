package io.primer.android.payment.async.retailOutlet

import io.primer.android.components.domain.core.mapper.PrimerPaymentMethodRawDataMapper
import io.primer.android.components.domain.core.mapper.retailOutlet.RetailOutletRawDataMapper
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.retailOutlet.PrimerRetailerData
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.di.extension.inject
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfoResolver
import io.primer.android.domain.payments.additionalInfo.RetailOutletsCheckoutAdditionalInfoResolver
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SDKCapability
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor

internal class XenditRetailOutletPaymentMethodDescriptor(
    override val options: AsyncPaymentMethod,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : AsyncPaymentMethodDescriptor(options, localConfig, config) {

    private val deeplinkRepository: AsyncPaymentMethodDeeplinkRepository by inject()

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val sdkCapabilities: List<SDKCapability>
        get() = listOf(SDKCapability.HEADLESS)

    override val additionalInfoResolver: PrimerCheckoutAdditionalInfoResolver
        get() = RetailOutletsCheckoutAdditionalInfoResolver()

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(
            listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            HeadlessDefinition.RawDataDefinition(
                PrimerRetailerData::class,
                RetailOutletRawDataMapper(deeplinkRepository, config, localConfig.settings)
                    as PrimerPaymentMethodRawDataMapper<PrimerRawData>
            )
        )
}
