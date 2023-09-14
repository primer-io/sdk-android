package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import androidx.lifecycle.SavedStateHandle
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayRequestPaymentInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayRequestPaymentParams
import io.primer.android.components.manager.nolPay.NolPayStartPaymentCollectableData
import io.primer.android.components.manager.nolPay.NolPayStartPaymentStep
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.nolpay.NolPayPaymentInstrumentParams
import io.primer.android.extensions.mapSuspendCatching
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest

internal class NolPayStartPaymentDelegate(
    private val tokenizationInteractor: TokenizationInteractor,
    private val requestPaymentInteractor: NolPayRequestPaymentInteractor
) {
    suspend fun handleCollectedCardData(
        collectedData: NolPayStartPaymentCollectableData,
        savedStateHandle: SavedStateHandle
    ): Result<NolPayStartPaymentStep> {
        return when (collectedData) {
            is NolPayStartPaymentCollectableData.NolPayPhoneData ->
                tokenizationInteractor.executeV2(
                    TokenizationParamsV2(
                        NolPayPaymentInstrumentParams(
                            PaymentMethodType.NOL_PAY.name,
                            collectedData.phoneCountryDiallingCode,
                            collectedData.mobileNumber,
                            requireNotNull(savedStateHandle.get<String>(PHYSICAL_CARD_KEY))
                        ),
                        PrimerSessionIntent.CHECKOUT
                    )
                ).mapLatest { Result.success(NolPayStartPaymentStep.COLLECT_TAG_DATA) }.first()

            is NolPayStartPaymentCollectableData.NolPayTagData -> requestPayment(
                collectedData,
                ""
            )

            is NolPayStartPaymentCollectableData.NolPayCardData -> {
                savedStateHandle[PHYSICAL_CARD_KEY] = collectedData.nolPaymentCard
                Result.success(NolPayStartPaymentStep.COLLECT_PHONE_DATA)
            }
        }
    }

    private suspend fun requestPayment(
        collectedData: NolPayStartPaymentCollectableData.NolPayTagData,
        transactionNo: String
    ) = requestPaymentInteractor(NolPayRequestPaymentParams(collectedData.tag, transactionNo))
        .mapSuspendCatching {
            NolPayStartPaymentStep.COLLECT_TAG_DATA
        }

    companion object {
        private const val PHYSICAL_CARD_KEY = "PHYSICAL_CARD"
        private const val LINKED_TOKEN_KEY = "LINKED_TOKEN"
        private const val REGION_CODE_KEY = "REGION_CODE"
        private const val MOBILE_NUMBER_KEY = "MOBILE_NUMBER"
    }
}
