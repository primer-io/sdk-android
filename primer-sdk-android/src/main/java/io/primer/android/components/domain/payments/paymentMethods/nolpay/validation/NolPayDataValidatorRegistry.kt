package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation

import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayMobileNumberDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayOtpDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayTagDataValidator
import io.primer.android.components.manager.nolPay.NolPayData
import kotlin.reflect.KClass

internal class NolPayDataValidatorRegistry {

    private val registry: Map<KClass<out NolPayData>, NolPayDataValidator<NolPayData>> =
        mapOf(
            NolPayData.NolPayOtpData::class to NolPayOtpDataValidator(),
            NolPayData.NolPayPhoneData::class to NolPayMobileNumberDataValidator(),
            NolPayData.NolPayTagData::class to NolPayTagDataValidator()
        )

    fun getValidator(data: NolPayData): NolPayDataValidator<NolPayData> =
        registry[data::class] ?: throw IllegalArgumentException(
            NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE
        )

    private companion object {

        const val NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE = "NolPay data validator not registered."
    }
}
