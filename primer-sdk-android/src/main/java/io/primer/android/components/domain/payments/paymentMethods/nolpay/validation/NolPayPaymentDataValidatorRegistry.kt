package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation

import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayPaymentTagDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayStartPaymentDataValidator
import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData
import io.primer.android.components.manager.nolPay.startPayment.composable.NolPayStartPaymentCollectableData
import kotlin.reflect.KClass

internal class NolPayPaymentDataValidatorRegistry {

    private val registry: Map<KClass<out NolPayStartPaymentCollectableData>,
        NolPayDataValidator<NolPayStartPaymentCollectableData>> =
        mapOf(
            NolPayStartPaymentCollectableData.NolPayStartPaymentData::class
                to NolPayStartPaymentDataValidator(),
            NolPayStartPaymentCollectableData.NolPayTagData::class to
                NolPayPaymentTagDataValidator(),
        )

    fun getValidator(data: NolPayCollectableData):
        NolPayDataValidator<NolPayStartPaymentCollectableData> = registry[data::class]
        ?: throw IllegalArgumentException(NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE)

    private companion object {

        const val NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE = "NolPay data validator not registered."
    }
}
