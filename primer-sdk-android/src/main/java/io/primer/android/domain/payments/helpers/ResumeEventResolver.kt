package io.primer.android.domain.payments.helpers

import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher

internal class ResumeEventResolver(
    private val config: PrimerConfig,
    private val resumeHandlerFactory: ResumeHandlerFactory,
    private val eventDispatcher: EventDispatcher
) {

    fun resolve(paymentInstrumentType: String, isVaulted: Boolean, resumeToken: String? = null) {
        when (config.settings.paymentHandling) {
            PrimerPaymentHandling.AUTO -> {
                val resumeEvent = when (config.settings.fromHUC) {
                    true -> when (isVaulted) {
                        true -> CheckoutEvent.ResumeSuccessInternalVaultHUC(
                            resumeToken.orEmpty(),
                            resumeHandlerFactory.getResumeHandler(paymentInstrumentType)
                        )
                        false -> CheckoutEvent.ResumeSuccessInternalHUC(
                            resumeToken.orEmpty(),
                            resumeHandlerFactory.getResumeHandler(paymentInstrumentType)
                        )
                    }
                    false -> CheckoutEvent.ResumeSuccessInternal(
                        resumeToken.orEmpty(),
                        resumeHandlerFactory.getResumeHandler(paymentInstrumentType)
                    )
                }

                eventDispatcher.dispatchEvent(resumeEvent)
            }
            PrimerPaymentHandling.MANUAL -> {
                val resumeEvent = when (config.settings.fromHUC) {
                    true -> CheckoutEvent.ResumeSuccessHUC(
                        resumeToken.orEmpty(),
                        resumeHandlerFactory.getResumeHandler(paymentInstrumentType)
                    )
                    false -> CheckoutEvent.ResumeSuccess(
                        resumeToken.orEmpty(),
                        resumeHandlerFactory.getResumeHandler(paymentInstrumentType)
                    )
                }
                eventDispatcher.dispatchEvent(resumeEvent)
            }
        }
    }
}
