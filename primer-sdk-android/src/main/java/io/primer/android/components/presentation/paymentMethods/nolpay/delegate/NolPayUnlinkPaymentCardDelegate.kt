package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import androidx.lifecycle.SavedStateHandle
import io.primer.android.components.data.payments.paymentMethods.nolpay.exception.NolPayIllegalValueKey
import io.primer.android.components.domain.payments.metadata.phone.PhoneMetadataInteractor
import io.primer.android.components.domain.payments.metadata.phone.model.PhoneMetadataParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetUnlinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPaySdkInitInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayUnlinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardOTPParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardParams
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCardStep
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.extensions.flatMap
import io.primer.android.extensions.mapSuspendCatching
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.api.models.PrimerNolPaymentCard

internal class NolPayUnlinkPaymentCardDelegate(
    private val unlinkPaymentCardOTPInteractor: NolPayGetUnlinkPaymentCardOTPInteractor,
    private val unlinkPaymentCardInteractor: NolPayUnlinkPaymentCardInteractor,
    private val phoneMetadataInteractor: PhoneMetadataInteractor,
    sdkInitInteractor: NolPaySdkInitInteractor
) : BaseNolPayDelegate(sdkInitInteractor) {

    suspend fun handleCollectedCardData(
        collectedData: NolPayUnlinkCollectableData?,
        savedStateHandle: SavedStateHandle
    ): Result<NolPayUnlinkCardStep> = runSuspendCatching {
        return when (
            val collectedDataUnwrapped =
                requireNotNullCheck(collectedData, NolPayIllegalValueKey.COLLECTED_DATA)
        ) {
            is NolPayUnlinkCollectableData.NolPayCardAndPhoneData -> {
                savedStateHandle[PHYSICAL_CARD_KEY] =
                    collectedDataUnwrapped.nolPaymentCard.cardNumber
                getPaymentCardOTP(collectedDataUnwrapped, savedStateHandle)
            }

            is NolPayUnlinkCollectableData.NolPayOtpData -> {
                unlinkPaymentCard(collectedDataUnwrapped, savedStateHandle)
            }
        }
    }

    private suspend fun getPaymentCardOTP(
        collectedData: NolPayUnlinkCollectableData.NolPayCardAndPhoneData,
        savedStateHandle: SavedStateHandle
    ) = phoneMetadataInteractor(PhoneMetadataParams(collectedData.mobileNumber))
        .flatMap { phoneMetadata ->
            unlinkPaymentCardOTPInteractor(
                NolPayUnlinkCardOTPParams(
                    phoneMetadata.nationalNumber,
                    phoneMetadata.countryCode,
                    requireNotNull(savedStateHandle[PHYSICAL_CARD_KEY])
                )
            )
        }.onSuccess {
            savedStateHandle[PHYSICAL_CARD_KEY] = it.cardNumber
            savedStateHandle[UNLINKED_TOKEN_KEY] = it.unlinkToken
        }.mapSuspendCatching { NolPayUnlinkCardStep.CollectOtpData }

    private suspend fun unlinkPaymentCard(
        collectedData: NolPayUnlinkCollectableData.NolPayOtpData,
        savedStateHandle: SavedStateHandle
    ) = unlinkPaymentCardInteractor(
        NolPayUnlinkCardParams(
            requireNotNull(savedStateHandle[PHYSICAL_CARD_KEY]),
            collectedData.otpCode,
            requireNotNull(savedStateHandle[UNLINKED_TOKEN_KEY])
        )
    ).mapSuspendCatching {
        NolPayUnlinkCardStep.CardUnlinked(
            PrimerNolPaymentCard(requireNotNull(savedStateHandle[PHYSICAL_CARD_KEY]))
        )
    }

    companion object {
        internal const val PHYSICAL_CARD_KEY = "PHYSICAL_CARD"
        internal const val UNLINKED_TOKEN_KEY = "UNLINKED_TOKEN"
    }
}
