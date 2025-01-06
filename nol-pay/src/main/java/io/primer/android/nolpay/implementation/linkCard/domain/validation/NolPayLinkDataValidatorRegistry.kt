package io.primer.android.nolpay.implementation.linkCard.domain.validation

import io.primer.android.core.di.extensions.resolve
import io.primer.android.nolpay.api.manager.core.composable.NolPayCollectableData
import io.primer.android.nolpay.api.manager.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.nolpay.implementation.validation.NolPayValidatorRegistry
import io.primer.android.nolpay.implementation.validation.validator.NolPayLinkMobileNumberDataValidator
import io.primer.android.nolpay.implementation.validation.validator.NolPayLinkOtpDataValidator
import io.primer.android.nolpay.implementation.validation.validator.NolPayLinkTagDataValidator
import io.primer.android.paymentmethods.CollectableDataValidator
import kotlin.reflect.KClass

internal class NolPayLinkDataValidatorRegistry : NolPayValidatorRegistry {
    private val registry:
        Map<
            KClass<out NolPayLinkCollectableData>,
            CollectableDataValidator<NolPayLinkCollectableData>,
            > =
        mapOf(
            NolPayLinkCollectableData.NolPayOtpData::class to NolPayLinkOtpDataValidator(),
            NolPayLinkCollectableData.NolPayPhoneData::class to
                NolPayLinkMobileNumberDataValidator(resolve()),
            NolPayLinkCollectableData.NolPayTagData::class to NolPayLinkTagDataValidator(),
        )

    override fun getValidator(data: NolPayCollectableData): CollectableDataValidator<NolPayCollectableData> =
        registry[data::class] ?: throw IllegalArgumentException(
            NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE,
        )

    private companion object {
        const val NOL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE = "NolPay data validator not registered."
    }
}
