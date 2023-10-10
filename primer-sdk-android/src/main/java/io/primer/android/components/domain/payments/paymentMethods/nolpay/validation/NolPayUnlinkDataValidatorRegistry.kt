package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation

import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayUnlinkCardAndMobileNumberDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayUnlinkOtpDataValidator
import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import kotlin.reflect.KClass

internal class NolPayUnlinkDataValidatorRegistry {

    private val registry: Map<KClass<out NolPayUnlinkCollectableData>,
        NolPayDataValidator<NolPayUnlinkCollectableData>> =
        mapOf(
            NolPayUnlinkCollectableData.NolPayOtpData::class to NolPayUnlinkOtpDataValidator(),
            NolPayUnlinkCollectableData.NolPayCardAndPhoneData::class to
                NolPayUnlinkCardAndMobileNumberDataValidator()
        )

    fun getValidator(data: NolPayCollectableData): NolPayDataValidator<NolPayCollectableData> =
        registry[data::class] ?: throw IllegalArgumentException(
            NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE
        )

    private companion object {

        const val NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE = "NolPay data validator not registered."
    }
}
