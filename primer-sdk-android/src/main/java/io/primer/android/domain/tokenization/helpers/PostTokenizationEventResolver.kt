package io.primer.android.domain.tokenization.helpers

import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.model.dto.PrimerPaymentHandling
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.TokenType

internal class PostTokenizationEventResolver(
    private val config: PrimerConfig,
    private val resumeHandlerFactory: ResumeHandlerFactory,
    private val eventDispatcher: EventDispatcher
) {

    fun resolve(token: PaymentMethodTokenInternal) {
        val externalToken = PaymentMethodTokenAdapter.internalToExternal(token)
        when {
            config.intent.paymentMethodIntent.isVault ||
                config.settings.options.paymentHandling == PrimerPaymentHandling.MANUAL -> {
                val events = mutableListOf<CheckoutEvent>(
                    CheckoutEvent.TokenizationSuccess(
                        externalToken,
                        resumeHandlerFactory.getResumeHandler(token.paymentInstrumentType),
                    )
                )
                if (token.tokenType == TokenType.MULTI_USE) {
                    events.add(CheckoutEvent.TokenAddedToVault(externalToken))
                }
                eventDispatcher.dispatchEvents(events)
            }
            config.settings.options.paymentHandling == PrimerPaymentHandling.AUTO -> {
                val events = mutableListOf<CheckoutEvent>()
                if (config.fromHUC) {
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
