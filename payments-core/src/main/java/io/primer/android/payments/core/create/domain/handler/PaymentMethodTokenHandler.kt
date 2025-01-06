package io.primer.android.payments.core.create.domain.handler

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.payments.core.create.domain.CreatePaymentInteractor
import io.primer.android.payments.core.create.domain.model.CreatePaymentParams
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.di.PaymentsContainer

fun interface PaymentHandlerStrategy {
    suspend fun handle(paymentMethodTokenData: PrimerPaymentMethodTokenData): Result<PaymentDecision>
}

internal class AutoPaymentHandlerStrategy(
    private val createPaymentInteractor: CreatePaymentInteractor,
) : PaymentHandlerStrategy {
    override suspend fun handle(paymentMethodTokenData: PrimerPaymentMethodTokenData) =
        createPaymentInteractor(CreatePaymentParams(token = paymentMethodTokenData.token))
}

internal class ManualPaymentHandlerStrategy(
    private val postTokenizationHandler: PostTokenizationHandler,
) : PaymentHandlerStrategy {
    override suspend fun handle(paymentMethodTokenData: PrimerPaymentMethodTokenData) =
        postTokenizationHandler.handle(paymentMethodTokenData)
}

/**
 * Handles payment creation or signals tokenization success, depending on the payment handling type (auto or manual). To
 * be called after with the token data resulting from the tokenization process.
 */
interface PaymentMethodTokenHandler {
    /**
     * Creates the payment or signals tokenization success depending on the payment handing type (auto or manual).
     */
    suspend fun handle(
        paymentMethodTokenData: PrimerPaymentMethodTokenData,
        primerSessionIntent: PrimerSessionIntent,
    ): Result<PaymentDecision>
}

class DefaultPaymentMethodTokenHandler(private val config: PrimerConfig) : PaymentMethodTokenHandler, DISdkComponent {
    private enum class PaymentHandlingStrategy {
        AUTO,
        MANUAL,
        VAULT,
    }

    private val strategies: Map<PaymentHandlingStrategy, PaymentHandlerStrategy> =
        mapOf(
            PaymentHandlingStrategy.AUTO to
                AutoPaymentHandlerStrategy(
                    resolve(name = PaymentsContainer.CREATE_PAYMENT_INTERACTOR_DI_KEY),
                ),
            PaymentHandlingStrategy.MANUAL to ManualPaymentHandlerStrategy(resolve()),
            PaymentHandlingStrategy.VAULT to ManualPaymentHandlerStrategy(resolve()),
        )

    override suspend fun handle(
        paymentMethodTokenData: PrimerPaymentMethodTokenData,
        primerSessionIntent: PrimerSessionIntent,
    ): Result<PaymentDecision> {
        val paymentHandlingStrategy =
            when {
                primerSessionIntent == PrimerSessionIntent.VAULT -> PaymentHandlingStrategy.VAULT
                config.settings.paymentHandling == PrimerPaymentHandling.MANUAL -> PaymentHandlingStrategy.MANUAL
                else -> PaymentHandlingStrategy.AUTO
            }
        return strategies[paymentHandlingStrategy]?.handle(paymentMethodTokenData)
            ?: error("Unregistered strategy for $paymentHandlingStrategy ")
    }
}
