package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation

import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayPaymentTagDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayPaymentCardAndMobileDataValidator
import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.resolve
import kotlin.reflect.KClass

internal class NolPayPaymentDataValidatorRegistry : DISdkComponent {

    private val registry: Map<KClass<out NolPayPaymentCollectableData>,
        NolPayDataValidator<NolPayPaymentCollectableData>> =
        mapOf(
            NolPayPaymentCollectableData.NolPayCardAndPhoneData::class
                to NolPayPaymentCardAndMobileDataValidator(resolve()),
            NolPayPaymentCollectableData.NolPayTagData::class to
                NolPayPaymentTagDataValidator()
        )

    fun getValidator(data: NolPayCollectableData):
        NolPayDataValidator<NolPayPaymentCollectableData> = registry[data::class]
        ?: throw IllegalArgumentException(NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE)

    private companion object {

        const val NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE = "NolPay data validator not registered."
    }
}
