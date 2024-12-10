package io.primer.android.payments.core.resume.domain.handler

import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.resume.domain.ResumePaymentInteractor
import io.primer.android.payments.core.resume.domain.models.ResumeParams
import io.primer.android.payments.di.PaymentsContainer

internal fun interface PaymentResumeHandlerStrategy {
    suspend fun handle(
        resumeToken: String,
        paymentId: String?
    ): Result<PaymentDecision>
}

internal class AutoPaymentResumeHandlerStrategy(
    private val resumePaymentInteractor: ResumePaymentInteractor
) : PaymentResumeHandlerStrategy {
    override suspend fun handle(
        resumeToken: String,
        paymentId: String?
    ): Result<PaymentDecision> =
        resumePaymentInteractor(
            ResumeParams(
                paymentId = requireNotNull(paymentId),
                resumeToken = resumeToken
            )
        )
}

internal class ManualPaymentResumeHandlerStrategy(
    private val postResumeHandler: PostResumeHandler
) : PaymentResumeHandlerStrategy {
    override suspend fun handle(
        resumeToken: String,
        paymentId: String?
    ): Result<PaymentDecision> = postResumeHandler.handle(resumeToken = resumeToken)
}

/**
 * Handles payment resume or signals checkout resume, depending on the payment handling type (auto or manual). To
 * be used by async payment method once they receive a resume token.
 */
interface PaymentResumeHandler {

    /**
     * Resumes the payment or signals checkout resume depending on the payment handing type (auto or manual).
     */
    suspend fun handle(
        resumeToken: String,
        paymentId: String?
    ): Result<PaymentDecision>
}

class DefaultPaymentResumeHandler(private val config: PrimerConfig) :
    PaymentResumeHandler,
    DISdkComponent {

    internal enum class ResumeHandlingStrategy {
        AUTO,
        MANUAL
    }

    private val strategies: Map<ResumeHandlingStrategy, PaymentResumeHandlerStrategy> = mapOf(
        ResumeHandlingStrategy.AUTO to AutoPaymentResumeHandlerStrategy(
            resumePaymentInteractor = resolve(PaymentsContainer.RESUME_PAYMENT_INTERACTOR_DI_KEY)
        ),
        ResumeHandlingStrategy.MANUAL to ManualPaymentResumeHandlerStrategy(resolve())
    )

    override suspend fun handle(
        resumeToken: String,
        paymentId: String?
    ): Result<PaymentDecision> {
        val paymentHandlingStrategy = when (config.settings.paymentHandling) {
            PrimerPaymentHandling.MANUAL -> ResumeHandlingStrategy.MANUAL
            else -> ResumeHandlingStrategy.AUTO
        }
        return strategies[paymentHandlingStrategy]?.handle(paymentId = paymentId, resumeToken = resumeToken)
            ?: error("Unregistered strategy for $paymentHandlingStrategy ")
    }
}
