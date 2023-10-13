package io.primer.android.payment.async.ovo

import io.primer.android.components.domain.core.mapper.PrimerPaymentMethodRawDataMapper
import io.primer.android.components.domain.core.mapper.phoneNumber.PhoneNumberRawDataMapper
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.phoneNumber.PrimerPhoneNumberData
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.di.extension.inject
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SDKCapability
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor

internal class XenditOvoPaymentMethodDescriptor(
    override val options: AsyncPaymentMethod,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : AsyncPaymentMethodDescriptor(options, localConfig, config) {

    private val deeplinkRepository: AsyncPaymentMethodDeeplinkRepository by inject()

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val sdkCapabilities: List<SDKCapability>
        get() = listOf(SDKCapability.HEADLESS)

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(
            listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            HeadlessDefinition.RawDataDefinition(
                PrimerPhoneNumberData::class,
                PhoneNumberRawDataMapper(deeplinkRepository, config, localConfig.settings)
                    as PrimerPaymentMethodRawDataMapper<PrimerRawData>
            )
        )
}
