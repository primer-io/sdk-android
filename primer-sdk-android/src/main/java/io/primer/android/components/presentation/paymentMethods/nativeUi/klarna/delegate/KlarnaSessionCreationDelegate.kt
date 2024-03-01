package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate

import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaSessionInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSessionParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.PrimerFee
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.domain.session.models.ConfigurationParams
import io.primer.android.extensions.flatMap
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.flow.last

internal class KlarnaSessionCreationDelegate(
    private val actionInteractor: ActionInteractor,
    private val interactor: KlarnaSessionInteractor,
    private val primerSettings: PrimerSettings,
    private val configurationInteractor: ConfigurationInteractor
) {
    suspend fun createSession(primerSessionIntent: PrimerSessionIntent): Result<KlarnaSession> =
        runSuspendCatching {
            Optional(getFees().firstOrNull { it.type == "SURCHARGE" }?.amount)
        }.flatMap { surcharge ->
            interactor.invoke(
                KlarnaSessionParams(
                    surcharge.value,
                    primerSessionIntent
                )
            )
        }

    private suspend fun getFees(): List<PrimerFee> =
        when (primerSettings.sdkIntegrationType) {
            SdkIntegrationType.DROP_IN ->
                configurationInteractor.invoke(ConfigurationParams(true))
                    .last().clientSession.clientSessionDataResponse.order?.toFees().orEmpty()
            SdkIntegrationType.HEADLESS ->
                actionInteractor(
                    ActionUpdateSelectPaymentMethodParams(
                        paymentMethodType = PaymentMethodType.KLARNA.name,
                        cardNetwork = null
                    )
                ).last().clientSession.fees.orEmpty()
        }

    private data class Optional(val value: Int?)
}
