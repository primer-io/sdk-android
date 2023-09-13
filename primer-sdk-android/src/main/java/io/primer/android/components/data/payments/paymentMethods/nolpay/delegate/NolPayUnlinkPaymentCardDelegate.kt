package io.primer.android.components.data.payments.paymentMethods.nolpay.delegate

import androidx.lifecycle.SavedStateHandle
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetUnlinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayUnlinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardOTPParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardParams
import io.primer.android.components.manager.nolPay.NolPayUnlinkCollectableData
import io.primer.android.components.manager.nolPay.NolPayUnlinkDataStep
import io.primer.android.extensions.mapSuspendCatching

internal class NolPayUnlinkPaymentCardDelegate(
    private val unlinkPaymentCardOTPInteractor: NolPayGetUnlinkPaymentCardOTPInteractor,
    private val unlinkPaymentCardInteractor: NolPayUnlinkPaymentCardInteractor,
) {
    suspend fun handleCollectedCardData(
        collectedData: NolPayUnlinkCollectableData,
        savedStateHandle: SavedStateHandle
    ): Result<NolPayUnlinkDataStep> {
        return when (collectedData) {
            is NolPayUnlinkCollectableData.NolPayPhoneData -> {
                getPaymentCardOTP(collectedData, savedStateHandle)
            }

            is NolPayUnlinkCollectableData.NolPayOtpData -> {
                unlinkPaymentCard(collectedData, savedStateHandle)
            }

            is NolPayUnlinkCollectableData.NolPayCardData -> {
                savedStateHandle[PHYSICAL_CARD_KEY] = collectedData.nolPaymentCard.cardNumber
                Result.success(NolPayUnlinkDataStep.COLLECT_PHONE_DATA)
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
        savedStateHandle[LINKED_TOKEN_KEY] = it.linkToken
    }.mapSuspendCatching { NolPayUnlinkDataStep.COLLECT_OTP_DATA }

    private suspend fun unlinkPaymentCard(
        collectedData: NolPayUnlinkCollectableData.NolPayOtpData,
        savedStateHandle: SavedStateHandle
    ) = unlinkPaymentCardInteractor(
        NolPayUnlinkCardParams(
            requireNotNull(savedStateHandle[LINKED_TOKEN_KEY]),
            requireNotNull(savedStateHandle[PHYSICAL_CARD_KEY]),
            collectedData.otpCode,
        )
    ).mapSuspendCatching {
        NolPayUnlinkDataStep.CARD_UNLINKED
    }

    companion object {
        private const val PHYSICAL_CARD_KEY = "PHYSICAL_CARD"
        private const val LINKED_TOKEN_KEY = "LINKED_TOKEN"
    }
}
