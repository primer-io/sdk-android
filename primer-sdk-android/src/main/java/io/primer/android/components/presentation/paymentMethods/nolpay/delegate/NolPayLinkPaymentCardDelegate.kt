package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import androidx.lifecycle.SavedStateHandle
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayLinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayLinkCardOTPParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayLinkCardParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayTagParams
import io.primer.android.components.manager.nolPay.NolPayLinkCardStep
import io.primer.android.components.manager.nolPay.NolPayLinkCollectableData
import io.primer.android.extensions.mapSuspendCatching
import io.primer.nolpay.models.PrimerNolPaymentCard

internal class NolPayLinkPaymentCardDelegate(
    private val nolPayGetLinkPaymentCardTokenInteractor: NolPayGetLinkPaymentCardTokenInteractor,
    private val nolPayGetLinkPaymentCardOTPInteractor: NolPayGetLinkPaymentCardOTPInteractor,
    private val nolPayLinkPaymentCardInteractor: NolPayLinkPaymentCardInteractor,
) {
    suspend fun handleCollectedCardData(
        collectedData: NolPayLinkCollectableData,
        savedStateHandle: SavedStateHandle
    ): Result<NolPayLinkCardStep> {
        return when (collectedData) {
            is NolPayLinkCollectableData.NolPayTagData -> {
                getPaymentCardLinkToken(collectedData, savedStateHandle)
            }

            is NolPayLinkCollectableData.NolPayPhoneData -> {
                getPaymentCardOTP(collectedData, savedStateHandle)
            }

            is NolPayLinkCollectableData.NolPayOtpData -> {
                linkPaymentCard(collectedData, savedStateHandle)
            }
        }
    }

    private suspend fun getPaymentCardLinkToken(
        collectedData: NolPayLinkCollectableData.NolPayTagData,
        savedStateHandle: SavedStateHandle
    ) = nolPayGetLinkPaymentCardTokenInteractor(NolPayTagParams(collectedData.tag))
        .onSuccess { linkToken ->
            savedStateHandle[PHYSICAL_CARD_KEY] = linkToken.cardNumber
            savedStateHandle[LINKED_TOKEN_KEY] = linkToken.linkToken
        }.mapSuspendCatching { metadata ->
            NolPayLinkCardStep.CollectPhoneData(metadata.cardNumber)
        }

    private suspend fun getPaymentCardOTP(
        collectedData: NolPayLinkCollectableData.NolPayPhoneData,
        savedStateHandle: SavedStateHandle
    ) = nolPayGetLinkPaymentCardOTPInteractor(
        NolPayLinkCardOTPParams(
            collectedData.mobileNumber,
            collectedData.phoneCountryDiallingCode,
            requireNotNull(savedStateHandle[LINKED_TOKEN_KEY])
        )
    ).onSuccess {
        savedStateHandle[REGION_CODE_KEY] =
            collectedData.phoneCountryDiallingCode
        savedStateHandle[MOBILE_NUMBER_KEY] =
            collectedData.mobileNumber
    }.mapSuspendCatching { NolPayLinkCardStep.CollectOtpData }

    private suspend fun linkPaymentCard(
        collectedData: NolPayLinkCollectableData.NolPayOtpData,
        savedStateHandle: SavedStateHandle
    ) = nolPayLinkPaymentCardInteractor(
        NolPayLinkCardParams(
            requireNotNull(savedStateHandle[LINKED_TOKEN_KEY]),
            collectedData.otpCode,
        )
    ).mapSuspendCatching {
        NolPayLinkCardStep.CardLinked(
            PrimerNolPaymentCard(
                savedStateHandle.get<String>(
                    PHYSICAL_CARD_KEY
                ).orEmpty()
            )
        )
    }

    companion object {
        private const val PHYSICAL_CARD_KEY = "PHYSICAL_CARD"
        private const val LINKED_TOKEN_KEY = "LINKED_TOKEN"
        private const val REGION_CODE_KEY = "REGION_CODE"
        private const val MOBILE_NUMBER_KEY = "MOBILE_NUMBER"
    }
}
