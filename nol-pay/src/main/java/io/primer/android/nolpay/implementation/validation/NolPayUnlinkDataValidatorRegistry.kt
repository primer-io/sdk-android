package io.primer.android.nolpay.implementation.validation

import io.primer.android.core.di.extensions.resolve
import io.primer.android.nolpay.api.manager.core.composable.NolPayCollectableData
import io.primer.android.nolpay.api.manager.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.nolpay.implementation.validation.validator.NolPayUnlinkCardAndMobileNumberDataValidator
import io.primer.android.nolpay.implementation.validation.validator.NolPayUnlinkOtpDataValidator
import io.primer.android.paymentmethods.CollectableDataValidator
import kotlin.reflect.KClass

internal class NolPayUnlinkDataValidatorRegistry : NolPayValidatorRegistry {

    private val registry: Map<KClass<out NolPayUnlinkCollectableData>,
        CollectableDataValidator<NolPayUnlinkCollectableData>> =
        mapOf(
            NolPayUnlinkCollectableData.NolPayOtpData::class to NolPayUnlinkOtpDataValidator(),
            NolPayUnlinkCollectableData.NolPayCardAndPhoneData::class to
                NolPayUnlinkCardAndMobileNumberDataValidator(resolve())
        )

    override fun getValidator(data: NolPayCollectableData): CollectableDataValidator<NolPayCollectableData> =
        registry[data::class] ?: throw IllegalArgumentException(
            NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE
        )

    private companion object {

        const val NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE = "NolPay data validator not registered."
    }
}
