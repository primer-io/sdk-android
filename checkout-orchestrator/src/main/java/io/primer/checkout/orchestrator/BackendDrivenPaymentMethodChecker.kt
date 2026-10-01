package io.primer.checkout.orchestrator

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BdcClientRequirementsNotMetContextParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodChecker
import io.primer.checkout.orchestrator.domain.model.ClientRequirementsCheck
import io.primer.checkout.orchestrator.domain.repository.ClientRequirementsRepository

/** Keeps a backend-driven method only when the state processor finds its client requirements met. */
internal class BackendDrivenPaymentMethodChecker(
    private val clientRequirementsRepository: ClientRequirementsRepository,
    private val settings: PrimerSettings,
    private val analyticsInteractor: AnalyticsInteractor,
    private val logReporter: LogReporter,
) : PaymentMethodChecker {

    override suspend fun shouldPaymentMethodBeAvailable(paymentMethod: PaymentMethod): Boolean {
        val type = paymentMethod.type
        val result = clientRequirementsRepository.getVerdicts()
        val verdict = result.getOrNull()?.get(type)
        when {
            result.isFailure -> logReporter.warn(
                "$type could not be checked against this SDK's capabilities, so it will not be available " +
                    "in this checkout. ${result.exceptionOrNull()?.message.orEmpty()}",
            )
            verdict == null -> logReporter.warn(
                "$type is not in the checked configuration, so it will not be available in this checkout.",
            )
            !verdict.satisfied -> reportUnmet(type, verdict)
            else -> logReporter.info("BDC: $type available")
        }
        return verdict?.satisfied == true
    }

    private suspend fun reportUnmet(type: String, verdict: ClientRequirementsCheck) {
        logReporter.warn(
            "$type was filtered out because this integration does not meet its requirements. " +
                "Please check the integration guide: $integrationGuide",
        )
        analyticsInteractor(
            MessageAnalyticsParams(
                messageType = MessageType.BDC_CLIENT_REQUIREMENTS_NOT_MET,
                message = "Client requirements not met for $type",
                severity = Severity.WARN,
                context = BdcClientRequirementsNotMetContextParams(paymentMethodType = type, unmet = verdict.unmet),
            ),
        )
    }

    private val integrationGuide: String
        get() = if (settings.sdkIntegrationType == SdkIntegrationType.HEADLESS) HEADLESS_GUIDE else DROP_IN_GUIDE

    private companion object {
        const val DROP_IN_GUIDE = "https://primer.io/docs/checkout/drop-in"
        const val HEADLESS_GUIDE = "https://primer.io/docs/checkout/headless"
    }
}
