package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation

import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayPaymentTagDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayStartPaymentDataValidator
import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData
import kotlin.reflect.KClass

internal class NolPayPaymentDataValidatorRegistry {

    private val registry: Map<KClass<out NolPayPaymentCollectableData>,
        NolPayDataValidator<NolPayPaymentCollectableData>> =
        mapOf(
            NolPayPaymentCollectableData.NolPayStartPaymentData::class
                to NolPayStartPaymentDataValidator(),
            NolPayPaymentCollectableData.NolPayTagData::class to
                NolPayPaymentTagDataValidator(),
        )

    fun getValidator(data: NolPayCollectableData):
        NolPayDataValidator<NolPayPaymentCollectableData> = registry[data::class]
        ?: throw IllegalArgumentException(NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE)

    private companion object {

        const val NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE = "NolPay data validator not registered."
    }
}
