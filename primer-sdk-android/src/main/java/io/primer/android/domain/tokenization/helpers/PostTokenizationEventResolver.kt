package io.primer.android.domain.tokenization.helpers

import io.primer.android.PrimerSessionIntent
import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.data.tokenization.models.TokenType
import io.primer.android.data.tokenization.models.toPaymentMethodToken
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher

internal class PostTokenizationEventResolver(
    private val config: PrimerConfig,
    private val resumeHandlerFactory: ResumeHandlerFactory,
    private val eventDispatcher: EventDispatcher
) {

    fun resolve(token: PaymentMethodTokenInternal, sessionIntent: PrimerSessionIntent? = null) {
        val externalToken = token.toPaymentMethodToken()
        when {
            config.intent.paymentMethodIntent.isVault ||
                config.settings.paymentHandling == PrimerPaymentHandling.MANUAL ||
                sessionIntent == PrimerSessionIntent.VAULT -> {
                val events = mutableListOf<CheckoutEvent>()
                val tokenizationEvent = when (config.settings.fromHUC) {
                    true -> CheckoutEvent.TokenizationSuccessHUC(
                        externalToken,
                        resumeHandlerFactory.getResumeHandler(token.paymentInstrumentType)
                    )
                    false -> CheckoutEvent.TokenizationSuccess(
                        externalToken,
                        resumeHandlerFactory.getResumeHandler(token.paymentInstrumentType)
                    )
                }
                events.add(tokenizationEvent)
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
                            )
                        )
                    )
                } else {
                    events.add(
                        CheckoutEvent.PaymentContinue(
                            externalToken,
                            resumeHandlerFactory.getResumeHandler(
                                token.paymentInstrumentType
                            )
                        )
                    )
                }
                eventDispatcher.dispatchEvents(events)
            }
        }
    }
}
