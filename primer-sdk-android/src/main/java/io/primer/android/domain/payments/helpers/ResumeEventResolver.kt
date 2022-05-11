package io.primer.android.domain.payments.helpers

import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.model.dto.PrimerPaymentHandling
import io.primer.android.model.dto.PrimerConfig

internal class ResumeEventResolver(
    private val config: PrimerConfig,
    private val resumeHandlerFactory: ResumeHandlerFactory,
    private val eventDispatcher: EventDispatcher,
) {

    fun resolve(paymentInstrumentType: String, resumeToken: String? = null) {
        when (config.settings.options.paymentHandling) {
            PrimerPaymentHandling.AUTO -> {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.ResumeSuccessInternal(
                        resumeToken.orEmpty(),
                        resumeHandlerFactory.getResumeHandler(paymentInstrumentType)
                    )
                )
            }
            PrimerPaymentHandling.MANUAL -> {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.ResumeSuccess(
                        resumeToken.orEmpty(),
                        resumeHandlerFactory.getResumeHandler(paymentInstrumentType)
                    )
                )
            }
        }
    }
}
