package io.primer.android.nolpay.implementation.validation

import io.primer.android.core.di.extensions.resolve
import io.primer.android.nolpay.api.manager.core.composable.NolPayCollectableData
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentCollectableData
import io.primer.android.nolpay.implementation.validation.validator.NolPayPaymentCardAndMobileDataValidator
import io.primer.android.nolpay.implementation.validation.validator.NolPayPaymentTagDataValidator
import io.primer.android.paymentmethods.CollectableDataValidator
import kotlin.reflect.KClass

internal class NolPayPaymentDataValidatorRegistry : NolPayValidatorRegistry {
    private val registry: Map<
        KClass<out NolPayPaymentCollectableData>,
        CollectableDataValidator<NolPayPaymentCollectableData>,
        > =
        mapOf(
            NolPayPaymentCollectableData.NolPayCardAndPhoneData::class
                to NolPayPaymentCardAndMobileDataValidator(resolve()),
            NolPayPaymentCollectableData.NolPayTagData::class to
                NolPayPaymentTagDataValidator(),
        )

    override fun getValidator(data: NolPayCollectableData): CollectableDataValidator<NolPayPaymentCollectableData> =
        registry[data::class]
            ?: throw IllegalArgumentException(NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE)

    private companion object {
        const val NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE = "NolPay data validator not registered."
    }
}
