package io.primer.android.klarna.implementation.tokenization.presentation

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable
import io.primer.android.klarna.implementation.session.domain.FinalizeKlarnaSessionInteractor
import io.primer.android.klarna.implementation.session.domain.KlarnaCustomerTokenInteractor
import io.primer.android.klarna.implementation.session.domain.models.KlarnaCustomerTokenParam
import io.primer.android.klarna.implementation.tokenization.domain.KlarnaTokenizationInteractor
import io.primer.android.klarna.implementation.tokenization.domain.model.KlarnaCheckoutPaymentInstrumentParams
import io.primer.android.klarna.implementation.tokenization.domain.model.KlarnaPaymentInstrumentParams
import io.primer.android.klarna.implementation.tokenization.domain.model.KlarnaVaultPaymentInstrumentParams

internal data class KlarnaTokenizationInputable(
    val sessionId: String,
    val authorizationToken: String,
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent
) : TokenizationInputable

internal class KlarnaTokenizationDelegate(
    private val klarnaCustomerTokenInteractor: KlarnaCustomerTokenInteractor,
    private val finalizeKlarnaSessionInteractor: FinalizeKlarnaSessionInteractor,
    tokenizationInteractor: KlarnaTokenizationInteractor
) : PaymentMethodTokenizationDelegate<KlarnaTokenizationInputable,
    KlarnaPaymentInstrumentParams>(tokenizationInteractor) {

    override suspend fun mapTokenizationData(input: KlarnaTokenizationInputable):
        Result<TokenizationParams<KlarnaPaymentInstrumentParams>> = runSuspendCatching {
        val tokenParams = KlarnaCustomerTokenParam(
            sessionId = input.sessionId,
            authorizationToken = input.authorizationToken
        )
        TokenizationParams(
            paymentInstrumentParams = when (input.primerSessionIntent) {
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
                    val data = finalizeKlarnaSessionInteractor.invoke(tokenParams).getOrThrow()
                    KlarnaCheckoutPaymentInstrumentParams(
                        klarnaAuthorizationToken = input.authorizationToken,
                        sessionData = data.sessionData
                    )
                }
            },
            sessionIntent = input.primerSessionIntent
        )
    }
}
