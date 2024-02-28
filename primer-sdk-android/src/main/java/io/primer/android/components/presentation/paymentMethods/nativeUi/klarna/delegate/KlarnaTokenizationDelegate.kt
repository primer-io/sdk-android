package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaCustomerTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.klarna.KlarnaPaymentInstrumentParams
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.last

internal class KlarnaTokenizationDelegate(
    private val actionInteractor: ActionInteractor,
    private val klarnaCustomerTokenInteractor: KlarnaCustomerTokenInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val primerConfig: PrimerConfig
) {
    suspend fun tokenize(sessionId: String, authorizationToken: String): Result<Unit> =
        runSuspendCatching {
            updateSelectedPaymentMethodParams()

            val customerTokenData = klarnaCustomerTokenInteractor.execute(
                KlarnaCustomerTokenParam(
                    sessionId = sessionId,
                    authorizationToken = authorizationToken
                )
            ).last()

            tokenizationInteractor.executeV2(
                params = TokenizationParamsV2(
                    paymentInstrumentParams = KlarnaPaymentInstrumentParams(
                        customerTokenData.customerTokenId,
                        customerTokenData.sessionData
                    ),
                    paymentMethodIntent = primerConfig.paymentMethodIntent
                )
            ).collect()
        }

    private suspend fun updateSelectedPaymentMethodParams() {
        actionInteractor(
            ActionUpdateSelectPaymentMethodParams(
                paymentMethodType = PaymentMethodType.KLARNA.name,
                cardNetwork = null
            )
        ).collect()
    }
}
