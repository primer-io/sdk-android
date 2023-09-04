package io.primer.android.components.data.payments.paymentMethods.nolpay.delegate

import androidx.lifecycle.SavedStateHandle
import com.snowballtech.transit.rta.Transit
import com.snowballtech.transit.rta.module.transit.TransitGetPhysicalCardRequest
import com.snowballtech.transit.rta.module.transit.TransitPhysicalCard
import io.primer.android.PrimerSessionIntent
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
import io.primer.android.components.manager.nolPay.NolPayStep
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.base.BaseErrorFlowResolver
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.nolpay.NolPayPaymentInstrumentParams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch

internal class NolPayLinkPaymentCardDelegate(
    private val nolPayGetLinkPaymentCardTokenInteractor: NolPayGetLinkPaymentCardTokenInteractor,
    private val nolPayGetLinkPaymentCardOTPInteractor: NolPayGetLinkPaymentCardOTPInteractor,
    private val nolPayLinkPaymentCardInteractor: NolPayLinkPaymentCardInteractor,
    private val nolPayUnlinkPaymentCardInteractor: NolPayUnlinkPaymentCardInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val errorFlowResolver: BaseErrorFlowResolver
) {
    suspend fun handleCollectedCardData(
        collectedData: NolPayData,
        stepFlow: MutableSharedFlow<NolPayStep>,
        errorFlow: MutableSharedFlow<PrimerError>,
        savedStateHandle: SavedStateHandle
    ) {
        when (collectedData) {
            is NolPayTagData -> {
                // comment out when unlinking card
                getLinkedPaymentCard(collectedData, savedStateHandle)
                getPaymentCardToken(collectedData, stepFlow, errorFlow, savedStateHandle)

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

    private fun getLinkedPaymentCard(
        collectedData: NolPayTagData,
        savedStateHandle: SavedStateHandle
    ) {
        savedStateHandle[PHYSICAL_CARD_KEY] =
            Transit.getTransitInstance().getPhysicalCard(
                TransitGetPhysicalCardRequest.Builder().setTag(collectedData.tag)
                    .build()
            )
    }

    private suspend fun getPaymentCardToken(
        collectedData: NolPayTagData,
        stepFlow: MutableSharedFlow<NolPayStep>,
        errorFlow: MutableSharedFlow<PrimerError>,
        savedStateHandle: SavedStateHandle

    ) {
        nolPayGetLinkPaymentCardTokenInteractor(NolPayTagParams(collectedData.tag))
            .onSuccess { linkToken ->
                savedStateHandle[LINKED_TOKEN_KEY] = linkToken
                stepFlow.emit(NolPayStep.COLLECT_PHONE_DATA)
            }.onFailure { throwable ->
                errorFlowResolver.resolve(throwable, errorFlow)
            }
    }

    private suspend fun getPaymentCardOTP(
        collectedData: NolPayPhoneData,
        stepFlow: MutableSharedFlow<NolPayStep>,
        errorFlow: MutableSharedFlow<PrimerError>,
        savedStateHandle: SavedStateHandle
    ) {
        nolPayGetLinkPaymentCardOTPInteractor(
            NolPayCardOTPParams(
                collectedData.mobileNumber,
                collectedData.phoneCountryCode,
                requireNotNull(savedStateHandle[LINKED_TOKEN_KEY])
            )
        ).onSuccess {
            savedStateHandle[REGION_CODE_KEY] =
                collectedData.phoneCountryCode
            savedStateHandle[MOBILE_NUMBER_KEY] =
                collectedData.mobileNumber
            stepFlow.emit(NolPayStep.COLLECT_OTP_DATA)
        }.onFailure { throwable ->
            errorFlowResolver.resolve(throwable, errorFlow)
        }
    }

    private suspend fun linkPaymentCard(
        collectedData: NolPayOtpData,
        stepFlow: MutableSharedFlow<NolPayStep>,
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
        stepFlow: MutableSharedFlow<NolPayStep>,
        errorFlow: MutableSharedFlow<PrimerError>
    ) {
        tokenizationInteractor.executeV2(
            TokenizationParamsV2(
                NolPayPaymentInstrumentParams(
                    PaymentMethodType.NOL_PAY.name,
                    Transit.getId(),
                    requireNotNull(savedStateHandle[REGION_CODE_KEY]),
                    requireNotNull(savedStateHandle[MOBILE_NUMBER_KEY]),
                    requireNotNull(
                        savedStateHandle.get<TransitPhysicalCard>(PHYSICAL_CARD_KEY)?.cardNumber
                    )
                ),
                PrimerSessionIntent.VAULT
            )
        ).catch {
            errorFlowResolver.resolve(it, errorFlow)
        }.collect {
            stepFlow.emit(NolPayStep.PAYMENT_TOKENIZED)
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
