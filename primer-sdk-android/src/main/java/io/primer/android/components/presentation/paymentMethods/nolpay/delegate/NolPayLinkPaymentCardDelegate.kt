package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import androidx.lifecycle.SavedStateHandle
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.components.data.payments.paymentMethods.nolpay.exception.NolPayIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayLinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayLinkCardOTPParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayLinkCardParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayTagParams
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCardStep
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.extensions.mapSuspendCatching
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.api.models.PrimerNolPaymentCard

internal class NolPayLinkPaymentCardDelegate(
    private val getLinkPaymentCardTokenInteractor: NolPayGetLinkPaymentCardTokenInteractor,
    private val getLinkPaymentCardOTPInteractor: NolPayGetLinkPaymentCardOTPInteractor,
    private val linkPaymentCardInteractor: NolPayLinkPaymentCardInteractor,
    analyticsInteractor: AnalyticsInteractor,
    appSecretInteractor: NolPayAppSecretInteractor,
    configurationInteractor: NolPayConfigurationInteractor
) : BaseNolPayDelegate(appSecretInteractor, configurationInteractor, analyticsInteractor) {

    suspend fun handleCollectedCardData(
        collectedData: NolPayLinkCollectableData?,
        savedStateHandle: SavedStateHandle
    ): Result<NolPayLinkCardStep> = runSuspendCatching {
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
        savedStateHandle: SavedStateHandle
    ) = getLinkPaymentCardTokenInteractor(NolPayTagParams(collectedData.tag))
        .onSuccess { linkToken ->
            savedStateHandle[PHYSICAL_CARD_KEY] = linkToken.cardNumber
            savedStateHandle[LINKED_TOKEN_KEY] = linkToken.linkToken
        }.mapSuspendCatching { metadata ->
            NolPayLinkCardStep.CollectPhoneData(metadata.cardNumber)
        }

    private suspend fun getPaymentCardOTP(
        collectedData: NolPayLinkCollectableData.NolPayPhoneData,
        savedStateHandle: SavedStateHandle
    ) = getLinkPaymentCardOTPInteractor(
        NolPayLinkCardOTPParams(
            collectedData.mobileNumber,
            collectedData.phoneCountryDiallingCode,
            requireNotNullCheck(
                savedStateHandle[LINKED_TOKEN_KEY],
                NolPayIllegalValueKey.SAVED_DATA_LINK_TOKEN
            )
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
    ) = linkPaymentCardInteractor(
        NolPayLinkCardParams(
            requireNotNullCheck(
                savedStateHandle[LINKED_TOKEN_KEY],
                NolPayIllegalValueKey.SAVED_DATA_LINK_TOKEN
            ),
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

        internal const val PHYSICAL_CARD_KEY = "PHYSICAL_CARD"
        internal const val LINKED_TOKEN_KEY = "LINKED_TOKEN"
        internal const val REGION_CODE_KEY = "REGION_CODE"
        internal const val MOBILE_NUMBER_KEY = "MOBILE_NUMBER"
    }
}
