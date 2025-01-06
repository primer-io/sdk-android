package io.primer.android.nolpay.implementation.linkCard.presentation

import androidx.lifecycle.SavedStateHandle
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.nolpay.api.manager.linkCard.composable.NolPayLinkCardStep
import io.primer.android.nolpay.api.manager.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.nolpay.implementation.common.domain.NolPaySdkInitInteractor
import io.primer.android.nolpay.implementation.common.domain.model.NolPayTagParams
import io.primer.android.nolpay.implementation.common.presentation.BaseNolPayDelegate
import io.primer.android.nolpay.implementation.errors.data.exception.NolPayIllegalValueKey
import io.primer.android.nolpay.implementation.linkCard.domain.NolPayGetLinkPaymentCardOTPInteractor
import io.primer.android.nolpay.implementation.linkCard.domain.NolPayGetLinkPaymentCardTokenInteractor
import io.primer.android.nolpay.implementation.linkCard.domain.NolPayLinkPaymentCardInteractor
import io.primer.android.nolpay.implementation.linkCard.domain.model.NolPayLinkCardOTPParams
import io.primer.android.nolpay.implementation.linkCard.domain.model.NolPayLinkCardParams
import io.primer.android.phoneMetadata.domain.PhoneMetadataInteractor
import io.primer.android.phoneMetadata.domain.model.PhoneMetadataParams
import io.primer.nolpay.api.models.PrimerNolPaymentCard

internal class NolPayLinkPaymentCardDelegate(
    private val getLinkPaymentCardTokenInteractor: NolPayGetLinkPaymentCardTokenInteractor,
    private val getLinkPaymentCardOTPInteractor: NolPayGetLinkPaymentCardOTPInteractor,
    private val linkPaymentCardInteractor: NolPayLinkPaymentCardInteractor,
    private val phoneMetadataInteractor: PhoneMetadataInteractor,
    override val sdkInitInteractor: NolPaySdkInitInteractor,
) : BaseNolPayDelegate {
    suspend fun handleCollectedCardData(
        collectedData: NolPayLinkCollectableData?,
        savedStateHandle: SavedStateHandle,
    ): Result<NolPayLinkCardStep> =
        runSuspendCatching {
            return when (
                val collectedDataUnwrapped =
                    requireNotNullCheck(collectedData, NolPayIllegalValueKey.COLLECTED_DATA)
            ) {
                is NolPayLinkCollectableData.NolPayTagData -> {
                    getPaymentCardLinkToken(collectedDataUnwrapped, savedStateHandle)
                }

                is NolPayLinkCollectableData.NolPayPhoneData -> {
                    getPaymentCardOTP(collectedDataUnwrapped, savedStateHandle)
                }

                is NolPayLinkCollectableData.NolPayOtpData -> {
                    linkPaymentCard(collectedDataUnwrapped, savedStateHandle)
                }
            }
        }

    private suspend fun getPaymentCardLinkToken(
        collectedData: NolPayLinkCollectableData.NolPayTagData,
        savedStateHandle: SavedStateHandle,
    ) = getLinkPaymentCardTokenInteractor(NolPayTagParams(collectedData.tag))
        .onSuccess { linkToken ->
            savedStateHandle[PHYSICAL_CARD_KEY] = linkToken.cardNumber
            savedStateHandle[LINKED_TOKEN_KEY] = linkToken.linkToken
        }.mapSuspendCatching { metadata ->
            NolPayLinkCardStep.CollectPhoneData(metadata.cardNumber)
        }

    private suspend fun getPaymentCardOTP(
        collectedData: NolPayLinkCollectableData.NolPayPhoneData,
        savedStateHandle: SavedStateHandle,
    ) = phoneMetadataInteractor(PhoneMetadataParams(collectedData.mobileNumber))
        .flatMap { phoneMetadata ->
            getLinkPaymentCardOTPInteractor(
                NolPayLinkCardOTPParams(
                    phoneMetadata.nationalNumber,
                    phoneMetadata.countryCode,
                    requireNotNullCheck(
                        savedStateHandle[LINKED_TOKEN_KEY],
                        NolPayIllegalValueKey.SAVED_DATA_LINK_TOKEN,
                    ),
                ),
            ).mapSuspendCatching {
                NolPayLinkCardStep.CollectOtpData(
                    collectedData.mobileNumber,
                )
            }
        }

    private suspend fun linkPaymentCard(
        collectedData: NolPayLinkCollectableData.NolPayOtpData,
        savedStateHandle: SavedStateHandle,
    ) = linkPaymentCardInteractor(
        NolPayLinkCardParams(
            requireNotNullCheck(
                savedStateHandle[LINKED_TOKEN_KEY],
                NolPayIllegalValueKey.SAVED_DATA_LINK_TOKEN,
            ),
            collectedData.otpCode,
        ),
    ).mapSuspendCatching {
        NolPayLinkCardStep.CardLinked(
            PrimerNolPaymentCard(
                savedStateHandle.get<String>(
                    PHYSICAL_CARD_KEY,
                ).orEmpty(),
            ),
        )
    }

    companion object {
        internal const val PHYSICAL_CARD_KEY = "PHYSICAL_CARD"
        internal const val LINKED_TOKEN_KEY = "LINKED_TOKEN"
    }
}
