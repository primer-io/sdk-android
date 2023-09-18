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
            is NolPayStartPaymentCollectableData.NolPayStartPaymentData ->
                tokenize(collectedData)

            is NolPayStartPaymentCollectableData.NolPayTagData -> requestPayment(
                collectedData,
                // TODO get the transactionNo
                ""
            )
        }
    }

    private suspend fun tokenize(collectedData: NolPayStartPaymentCollectableData.NolPayStartPaymentData) = tokenizationInteractor.executeV2(
        TokenizationParamsV2(
            NolPayPaymentInstrumentParams(
                PaymentMethodType.NOL_PAY.name,
                collectedData.phoneCountryDiallingCode,
                collectedData.mobileNumber,
                collectedData.nolPaymentCard.cardNumber
            ),
            PrimerSessionIntent.CHECKOUT
        )
    ).mapLatest { Result.success(NolPayStartPaymentStep.CollectTagData) }.first()

    private suspend fun requestPayment(
        collectedData: NolPayStartPaymentCollectableData.NolPayTagData,
        transactionNo: String
    ) = requestPaymentInteractor(NolPayRequestPaymentParams(collectedData.tag, transactionNo))
        .mapSuspendCatching {
            NolPayStartPaymentStep.CollectTagData
        }
}
