package io.primer.android.nolpay.implementation.unlinkCard.presentation

import androidx.lifecycle.SavedStateHandle
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.nolpay.api.manager.unlinkCard.composable.NolPayUnlinkCardStep
import io.primer.android.nolpay.api.manager.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.nolpay.implementation.common.domain.NolPaySdkInitInteractor
import io.primer.android.nolpay.implementation.common.presentation.BaseNolPayDelegate
import io.primer.android.nolpay.implementation.errors.data.exception.NolPayIllegalValueKey
import io.primer.android.nolpay.implementation.unlinkCard.domain.NolPayGetUnlinkPaymentCardOTPInteractor
import io.primer.android.nolpay.implementation.unlinkCard.domain.NolPayUnlinkPaymentCardInteractor
import io.primer.android.nolpay.implementation.unlinkCard.domain.model.NolPayUnlinkCardOTPParams
import io.primer.android.nolpay.implementation.unlinkCard.domain.model.NolPayUnlinkCardParams
import io.primer.android.phoneMetadata.domain.PhoneMetadataInteractor
import io.primer.android.phoneMetadata.domain.model.PhoneMetadataParams
import io.primer.nolpay.api.models.PrimerNolPaymentCard

internal class NolPayUnlinkPaymentCardDelegate(
    private val unlinkPaymentCardOTPInteractor: NolPayGetUnlinkPaymentCardOTPInteractor,
    private val unlinkPaymentCardInteractor: NolPayUnlinkPaymentCardInteractor,
    private val phoneMetadataInteractor: PhoneMetadataInteractor,
    override val sdkInitInteractor: NolPaySdkInitInteractor,
) : BaseNolPayDelegate {
    suspend fun handleCollectedCardData(
        collectedData: NolPayUnlinkCollectableData?,
        savedStateHandle: SavedStateHandle,
    ): Result<NolPayUnlinkCardStep> =
        runSuspendCatching {
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
        savedStateHandle: SavedStateHandle,
    ) = phoneMetadataInteractor(PhoneMetadataParams(collectedData.mobileNumber))
        .flatMap { phoneMetadata ->
            unlinkPaymentCardOTPInteractor(
                NolPayUnlinkCardOTPParams(
                    phoneMetadata.nationalNumber,
                    phoneMetadata.countryCode,
                    requireNotNull(savedStateHandle[PHYSICAL_CARD_KEY]),
                ),
            )
        }.onSuccess {
            savedStateHandle[PHYSICAL_CARD_KEY] = it.cardNumber
            savedStateHandle[UNLINKED_TOKEN_KEY] = it.unlinkToken
        }.mapSuspendCatching { NolPayUnlinkCardStep.CollectOtpData }

    private suspend fun unlinkPaymentCard(
        collectedData: NolPayUnlinkCollectableData.NolPayOtpData,
        savedStateHandle: SavedStateHandle,
    ) = unlinkPaymentCardInteractor(
        NolPayUnlinkCardParams(
            requireNotNull(savedStateHandle[PHYSICAL_CARD_KEY]),
            collectedData.otpCode,
            requireNotNull(savedStateHandle[UNLINKED_TOKEN_KEY]),
        ),
    ).mapSuspendCatching {
        NolPayUnlinkCardStep.CardUnlinked(
            PrimerNolPaymentCard(requireNotNull(savedStateHandle[PHYSICAL_CARD_KEY])),
        )
    }

    companion object {
        internal const val PHYSICAL_CARD_KEY = "PHYSICAL_CARD"
        internal const val UNLINKED_TOKEN_KEY = "UNLINKED_TOKEN"
    }
}
