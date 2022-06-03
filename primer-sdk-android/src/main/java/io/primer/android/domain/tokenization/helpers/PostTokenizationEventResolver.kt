package io.primer.android.domain.tokenization.helpers

import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.tokenization.models.TokenType
import io.primer.android.data.tokenization.models.toPaymentMethodToken

internal class PostTokenizationEventResolver(
    private val config: PrimerConfig,
    private val resumeHandlerFactory: ResumeHandlerFactory,
    private val eventDispatcher: EventDispatcher
) {

    fun resolve(token: PaymentMethodTokenInternal) {
        val externalToken = token.toPaymentMethodToken()
        when {
            config.intent.paymentMethodIntent.isVault ||
                config.settings.paymentHandling == PrimerPaymentHandling.MANUAL -> {
                val events = mutableListOf<CheckoutEvent>(
                    CheckoutEvent.TokenizationSuccess(
                        externalToken,
                        resumeHandlerFactory.getResumeHandler(token.paymentInstrumentType),
                    )
                )
                if (token.tokenType == TokenType.MULTI_USE) {
                    events.add(CheckoutEvent.TokenAddedToVaultInternal(externalToken))
                }
                eventDispatcher.dispatchEvents(events)
            }
            config.settings.paymentHandling == PrimerPaymentHandling.AUTO -> {
                val events = mutableListOf<CheckoutEvent>()
                if (config.settings.fromHUC) {
                    events.add(
                        CheckoutEvent.PaymentContinueHUC(
                            externalToken,
                            resumeHandlerFactory.getResumeHandler(
                                token.paymentInstrumentType
                            ),
                        )
                    )
                } else {
                    events.add(
                        CheckoutEvent.PaymentContinue(
                            externalToken,
                            resumeHandlerFactory.getResumeHandler(
                                token.paymentInstrumentType
                            ),
                        )
                    )
                }
                eventDispatcher.dispatchEvents(events)
            }
        }
    }
}
