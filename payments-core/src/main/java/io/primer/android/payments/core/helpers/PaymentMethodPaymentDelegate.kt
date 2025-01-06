package io.primer.android.payments.core.helpers

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.extensions.flatMap
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler

abstract class PaymentMethodPaymentDelegate(
    private val paymentMethodTokenHandler: PaymentMethodTokenHandler,
    private val resumePaymentHandler: PaymentResumeHandler,
    private val successHandler: CheckoutSuccessHandler,
    private val errorHandler: CheckoutErrorHandler,
    private val baseErrorResolver: BaseErrorResolver,
) {
    private var payment: Payment? = null

    /**
     * Creates the payment or signals tokenization success depending on the payment handing type (auto or manual).
     * The function continues with handling of the payment decision by either dispatching checkout failure,
     * checkout success, or (if the payment is pending) by delegating the handling decision to the subclass
     * implementation via [handleNewClientToken].
     */
    open suspend fun handlePaymentMethodToken(
        paymentMethodTokenData: PrimerPaymentMethodTokenData,
        primerSessionIntent: PrimerSessionIntent,
    ): Result<PaymentDecision> =
        paymentMethodTokenHandler.handle(
            paymentMethodTokenData = paymentMethodTokenData,
            primerSessionIntent = primerSessionIntent,
        ).flatMap { decision ->
            payment = decision.payment
            when (decision) {
                is PaymentDecision.Error -> {
                    errorHandler.handle(
                        error = decision.error,
                        payment = decision.payment,
                    )
                    Result.success(Unit)
                }

                is PaymentDecision.Pending ->
                    handleNewClientToken(
                        clientToken = decision.clientToken,
                        payment = payment,
                    )

                is PaymentDecision.Success -> {
                    successHandler.handle(
                        payment = decision.payment,
                        additionalInfo = null,
                    )
                    Result.success(Unit)
                }
            }.map { decision }
        }

    /**
     * Called by asynchronous payment methods whenever the payment status changes.
     */
    open suspend fun resumePayment(resumeToken: String) =
        resumePaymentHandler.handle(resumeToken = resumeToken, paymentId = payment?.id)
            .onSuccess { decision ->
                when (decision) {
                    is PaymentDecision.Error -> {
                        errorHandler.handle(
                            error = decision.error,
                            payment = decision.payment,
                        )
                        Result.success(Unit)
                    }

                    is PaymentDecision.Pending ->
                        handleNewClientToken(
                            clientToken = decision.clientToken,
                            payment = payment,
                        )

                    is PaymentDecision.Success -> {
                        successHandler.handle(
                            payment = decision.payment,
                            additionalInfo = null,
                        )
                        Result.success(Unit)
                    }
                }.map { decision }
            }
            .onFailure {
                errorHandler.handle(
                    error = baseErrorResolver.resolve(it),
                    payment = null,
                )
            }

    /**
     * Called when the [payment decision][PaymentDecision] is in a [pending][PaymentDecision.Pending] state.
     */
    abstract suspend fun handleNewClientToken(
        clientToken: String,
        payment: Payment?,
    ): Result<Unit>

    /**
     * Dispatches the given [Throwable] as a checkout failure, logging it to analytics.
     */
    suspend fun handleError(throwable: Throwable) =
        errorHandler.handle(error = baseErrorResolver.resolve(throwable), payment = payment)
}
