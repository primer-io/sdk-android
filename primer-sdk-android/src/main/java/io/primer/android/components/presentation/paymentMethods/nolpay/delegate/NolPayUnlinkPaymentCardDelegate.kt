package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import androidx.lifecycle.SavedStateHandle
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetUnlinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayUnlinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardOTPParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardParams
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCardStep
import io.primer.android.extensions.mapSuspendCatching
import io.primer.nolpay.models.PrimerNolPaymentCard

internal class NolPayUnlinkPaymentCardDelegate(
    private val unlinkPaymentCardOTPInteractor: NolPayGetUnlinkPaymentCardOTPInteractor,
    private val unlinkPaymentCardInteractor: NolPayUnlinkPaymentCardInteractor,
) {
    suspend fun handleCollectedCardData(
        collectedData: NolPayUnlinkCollectableData,
        savedStateHandle: SavedStateHandle
    ): Result<NolPayUnlinkCardStep> {
        return when (collectedData) {
            is NolPayUnlinkCollectableData.NolPayPhoneData -> {
                getPaymentCardOTP(collectedData, savedStateHandle)
            }

            is NolPayUnlinkCollectableData.NolPayOtpData -> {
                unlinkPaymentCard(collectedData, savedStateHandle)
            }

            is NolPayUnlinkCollectableData.NolPayCardData -> {
                savedStateHandle[PHYSICAL_CARD_KEY] = collectedData.nolPaymentCard.cardNumber
                Result.success(NolPayUnlinkCardStep.CollectPhoneData)
            }
        }
    }

    private suspend fun getPaymentCardOTP(
        collectedData: NolPayUnlinkCollectableData.NolPayPhoneData,
        savedStateHandle: SavedStateHandle
    ) = unlinkPaymentCardOTPInteractor(
        NolPayUnlinkCardOTPParams(
            collectedData.mobileNumber,
            collectedData.phoneCountryDiallingCode,
            requireNotNull(savedStateHandle[PHYSICAL_CARD_KEY])
        )
    ).onSuccess {
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
            requireNotNull(savedStateHandle[UNLINKED_TOKEN_KEY]),
        )
    ).mapSuspendCatching {
        NolPayUnlinkCardStep.CardUnlinked(
            PrimerNolPaymentCard(requireNotNull(savedStateHandle[PHYSICAL_CARD_KEY]))
        )
    }

    companion object {
        private const val PHYSICAL_CARD_KEY = "PHYSICAL_CARD"
        private const val UNLINKED_TOKEN_KEY = "UNLINKED_TOKEN"
    }
}
