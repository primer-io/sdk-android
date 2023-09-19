package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation

import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayCardNumberValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayMobileNumberDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayOtpDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayTagDataValidator
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData
import kotlin.reflect.KClass

internal class NolPayDataValidatorRegistry {

    private val registry:
        Map<KClass<out NolPayCollectableData>, NolPayDataValidator<NolPayCollectableData>> =
        mapOf(
            NolPayLinkCollectableData.NolPayOtpData::class to NolPayOtpDataValidator(),
            NolPayLinkCollectableData.NolPayPhoneData::class to NolPayMobileNumberDataValidator(),
            NolPayLinkCollectableData.NolPayTagData::class to NolPayTagDataValidator(),
            NolPayUnlinkCollectableData.NolPayOtpData::class to NolPayOtpDataValidator(),
            NolPayUnlinkCollectableData.NolPayPhoneData::class to NolPayMobileNumberDataValidator(),
            NolPayUnlinkCollectableData.NolPayCardData::class to NolPayCardNumberValidator()
        )

    fun getValidator(data: NolPayCollectableData): NolPayDataValidator<NolPayCollectableData> =
        registry[data::class] ?: throw IllegalArgumentException(
            NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE
        )

    private companion object {

        const val NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE = "NolPay data validator not registered."
    }
}
