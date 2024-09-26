package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation

import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayLinkMobileNumberDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayLinkOtpDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayLinkTagDataValidator
import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.di.extension.resolve
import kotlin.reflect.KClass

internal class NolPayLinkDataValidatorRegistry : NolPayValidatorRegistry {

    private val registry:
        Map<KClass<out NolPayLinkCollectableData>,
            NolPayDataValidator<NolPayLinkCollectableData>> = mapOf(
            NolPayLinkCollectableData.NolPayOtpData::class to NolPayLinkOtpDataValidator(),
            NolPayLinkCollectableData.NolPayPhoneData::class to
                NolPayLinkMobileNumberDataValidator(resolve()),
            NolPayLinkCollectableData.NolPayTagData::class to NolPayLinkTagDataValidator()
        )

    override fun getValidator(data: NolPayCollectableData): NolPayDataValidator<NolPayCollectableData> =
        registry[data::class] ?: throw IllegalArgumentException(
            NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE
        )

    private companion object {

        const val NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE = "NolPay data validator not registered."
    }
}
