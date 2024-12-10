package io.primer.android.vault.implementation.vaultedMethods.domain

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.tokenization.data.model.toPaymentMethodToken
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.vault.implementation.vaultedMethods.domain.model.VaultTokenParams
import io.primer.android.vault.implementation.vaultedMethods.domain.repository.VaultedPaymentMethodExchangeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class VaultedPaymentMethodsExchangeInteractor(
    private val vaultedPaymentMethodExchangeRepository: VaultedPaymentMethodExchangeRepository,
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    private val preTokenizationHandler: PreTokenizationHandler,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PrimerPaymentMethodTokenData, VaultTokenParams>() {

    override suspend fun performAction(params: VaultTokenParams): Result<PrimerPaymentMethodTokenData> {
        val paymentMethodType = params.paymentMethodType
        return preTokenizationHandler.handle(
            paymentMethodType = paymentMethodType,
            sessionIntent = PrimerSessionIntent.CHECKOUT
        ).flatMap {
            logReporter.info(
                "Started token exchange for $paymentMethodType payment method."
            )
            vaultedPaymentMethodExchangeRepository.exchangeVaultedPaymentToken(
                id = params.vaultedPaymentMethodId,
                additionalData = params.additionalData
            ).onSuccess { paymentMethodToken ->
                logReporter.info(
                    "Token exchange successful for $paymentMethodType payment method."
                )
                tokenizedPaymentMethodRepository.setPaymentMethod(paymentMethodToken)
            }
        }.map { paymentMethodToken -> paymentMethodToken.toPaymentMethodToken() }
    }
}
