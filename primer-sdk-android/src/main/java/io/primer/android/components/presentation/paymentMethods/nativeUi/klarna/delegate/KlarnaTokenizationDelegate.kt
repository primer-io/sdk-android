package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate

import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.FinalizeKlarnaSessionInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaCustomerTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.klarna.KlarnaCheckoutPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.klarna.KlarnaVaultPaymentInstrumentParams
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.flow.collect

internal class KlarnaTokenizationDelegate(
    private val klarnaCustomerTokenInteractor: KlarnaCustomerTokenInteractor,
    private val finalizeKlarnaSessionInteractor: FinalizeKlarnaSessionInteractor,
    private val tokenizationInteractor: TokenizationInteractor
) {
    suspend fun tokenize(
        sessionId: String,
        authorizationToken: String,
        primerSessionIntent: PrimerSessionIntent
    ): Result<Unit> =
        runSuspendCatching {
            val tokenParams = KlarnaCustomerTokenParam(
                sessionId = sessionId,
                authorizationToken = authorizationToken
            )

            tokenizationInteractor.executeV2(
                params = TokenizationParamsV2(
                    paymentInstrumentParams = when (primerSessionIntent) {
                        PrimerSessionIntent.VAULT -> {
                            val customerTokenData =
                                klarnaCustomerTokenInteractor.invoke(tokenParams)
                                    .getOrThrow()
                            KlarnaVaultPaymentInstrumentParams(
                                klarnaCustomerToken = customerTokenData.customerTokenId,
                                sessionData = customerTokenData.sessionData
                            )
                        }

                        PrimerSessionIntent.CHECKOUT -> {
                            val data = finalizeKlarnaSessionInteractor.invoke(tokenParams)
                                .getOrThrow()
                            KlarnaCheckoutPaymentInstrumentParams(
                                klarnaAuthorizationToken = authorizationToken,
                                sessionData = data.sessionData
                            )
                        }
                    },
                    paymentMethodIntent = primerSessionIntent
                )
            ).collect()
        }
}
