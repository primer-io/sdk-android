package io.primer.android.components.data.payments.paymentMethods.nolpay.delegate

import androidx.lifecycle.SavedStateHandle
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetCardDetailsInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayLinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayUnlinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayCardOTPParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayLinkCardParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayTagParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardParams
import io.primer.android.components.manager.nolPay.NolPayData
import io.primer.android.components.manager.nolPay.NolPayData.NolPayOtpData
import io.primer.android.components.manager.nolPay.NolPayData.NolPayPhoneData
import io.primer.android.components.manager.nolPay.NolPayData.NolPayTagData
import io.primer.android.components.manager.nolPay.NolPayCollectDataStep
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.base.BaseErrorFlowResolver
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.nolpay.NolPayPaymentInstrumentParams
import io.primer.android.extensions.flatMap
import io.primer.nolpay.PrimerNolPay
import io.primer.nolpay.models.PrimerNolPaymentCard
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch

internal class NolPayLinkPaymentCardDelegate(
    private val nolPayGetLinkPaymentCardTokenInteractor: NolPayGetLinkPaymentCardTokenInteractor,
    private val nolPayGetLinkPaymentCardOTPInteractor: NolPayGetLinkPaymentCardOTPInteractor,
    private val nolPayLinkPaymentCardInteractor: NolPayLinkPaymentCardInteractor,
    private val nolPayUnlinkPaymentCardInteractor: NolPayUnlinkPaymentCardInteractor,
    private val nolPayGetCardDetailsInteractor: NolPayGetCardDetailsInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val errorFlowResolver: BaseErrorFlowResolver
) {
    suspend fun handleCollectedCardData(
        collectedData: NolPayData,
        stepFlow: MutableSharedFlow<NolPayCollectDataStep>,
        errorFlow: MutableSharedFlow<PrimerError>,
        savedStateHandle: SavedStateHandle
    ) {
        when (collectedData) {
            is NolPayTagData -> {
                // comment out when unlinking card
                getLinkedPaymentCard(collectedData, savedStateHandle).flatMap {
                    getPaymentCardToken(collectedData, stepFlow, errorFlow, savedStateHandle)
                }

                // comment out when linking card
                // unlinkPaymentCard()
            }

            is NolPayPhoneData -> {
                getPaymentCardOTP(collectedData, stepFlow, errorFlow, savedStateHandle)
            }

            is NolPayOtpData -> {
                linkPaymentCard(collectedData, stepFlow, errorFlow, savedStateHandle)
            }
        }
    }

    private suspend fun getLinkedPaymentCard(
        collectedData: NolPayTagData,
        savedStateHandle: SavedStateHandle
    ) = nolPayGetCardDetailsInteractor(NolPayTagParams(collectedData.tag)).onSuccess {
        savedStateHandle[PHYSICAL_CARD_KEY] = it
    }

    private suspend fun getPaymentCardToken(
        collectedData: NolPayTagData,
        stepFlow: MutableSharedFlow<NolPayCollectDataStep>,
        errorFlow: MutableSharedFlow<PrimerError>,
        savedStateHandle: SavedStateHandle
    ) = nolPayGetLinkPaymentCardTokenInteractor(NolPayTagParams(collectedData.tag))
        .onSuccess { linkToken ->
            savedStateHandle[LINKED_TOKEN_KEY] = linkToken
            stepFlow.emit(NolPayCollectDataStep.COLLECT_PHONE_DATA)
        }.onFailure { throwable ->
            errorFlowResolver.resolve(throwable, errorFlow)
        }

    private suspend fun getPaymentCardOTP(
        collectedData: NolPayPhoneData,
        stepFlow: MutableSharedFlow<NolPayCollectDataStep>,
        errorFlow: MutableSharedFlow<PrimerError>,
        savedStateHandle: SavedStateHandle
    ) {
        nolPayGetLinkPaymentCardOTPInteractor(
            NolPayCardOTPParams(
                collectedData.mobileNumber,
                collectedData.phoneCountryDiallingCode,
                requireNotNull(savedStateHandle[LINKED_TOKEN_KEY])
            )
        ).onSuccess {
            savedStateHandle[REGION_CODE_KEY] =
                collectedData.phoneCountryDiallingCode
            savedStateHandle[MOBILE_NUMBER_KEY] =
                collectedData.mobileNumber
            stepFlow.emit(NolPayCollectDataStep.COLLECT_OTP_DATA)
        }.onFailure { throwable ->
            errorFlowResolver.resolve(throwable, errorFlow)
        }
    }

    private suspend fun linkPaymentCard(
        collectedData: NolPayOtpData,
        stepFlow: MutableSharedFlow<NolPayCollectDataStep>,
        errorFlow: MutableSharedFlow<PrimerError>,
        savedStateHandle: SavedStateHandle
    ) {
        nolPayLinkPaymentCardInteractor(
            NolPayLinkCardParams(
                collectedData.otpCode,
                requireNotNull(savedStateHandle[LINKED_TOKEN_KEY])
            )
        ).onSuccess {
            tokenize(savedStateHandle, stepFlow, errorFlow)
        }.onFailure { throwable ->
            errorFlowResolver.resolve(throwable, errorFlow)
        }
    }

    private suspend fun tokenize(
        savedStateHandle: SavedStateHandle,
        stepFlow: MutableSharedFlow<NolPayCollectDataStep>,
        errorFlow: MutableSharedFlow<PrimerError>
    ) {
        tokenizationInteractor.executeV2(
            TokenizationParamsV2(
                NolPayPaymentInstrumentParams(
                    PaymentMethodType.NOL_PAY.name,
                    PrimerNolPay.instance.getSdkId(),
                    requireNotNull(savedStateHandle[REGION_CODE_KEY]),
                    requireNotNull(savedStateHandle[MOBILE_NUMBER_KEY]),
                    requireNotNull(
                        savedStateHandle.get<PrimerNolPaymentCard>(PHYSICAL_CARD_KEY)?.cardNumber
                    )
                ),
                PrimerSessionIntent.VAULT
            )
        ).catch {
            errorFlowResolver.resolve(it, errorFlow)
        }.collect {
        }
    }

    private suspend fun unlinkPaymentCard() {
        nolPayUnlinkPaymentCardInteractor(
            NolPayUnlinkCardParams(
                "7724463932",
                "44",
                "313971020",
                "000000"
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
