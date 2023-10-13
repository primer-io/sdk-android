package io.primer.android.components.domain.tokenization.helpers

import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.data.tokenization.models.toPaymentMethodToken
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher

internal class VaultPostTokenizationEventResolver(
    private val config: PrimerConfig,
    private val resumeHandlerFactory: ResumeHandlerFactory,
    private val eventDispatcher: EventDispatcher
) {

    fun resolve(token: PaymentMethodTokenInternal) {
        val externalToken = token.toPaymentMethodToken()
        when (config.settings.paymentHandling) {
            PrimerPaymentHandling.MANUAL -> {
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
                eventDispatcher.dispatchEvents(events)
            }
            PrimerPaymentHandling.AUTO -> {
                val events = mutableListOf<CheckoutEvent>()
                events.add(
                    CheckoutEvent.PaymentContinueVaultHUC(
                        externalToken,
                        resumeHandlerFactory.getResumeHandler(
                            token.paymentInstrumentType
                        )
                    )
                )
                eventDispatcher.dispatchEvents(events)
            }
        }
    }
}
