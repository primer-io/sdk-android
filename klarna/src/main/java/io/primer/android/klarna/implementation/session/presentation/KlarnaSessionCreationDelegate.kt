package io.primer.android.klarna.implementation.session.presentation

import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.PrimerSessionIntent
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.domain.action.models.PrimerFee
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.klarna.implementation.session.domain.KlarnaSessionInteractor
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSession
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSessionParams

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
                configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache)).getOrThrow()
                    .clientSession.clientSessionDataResponse.order?.toFees().orEmpty()

            SdkIntegrationType.HEADLESS ->
                actionInteractor(
                    MultipleActionUpdateParams(
                        listOf(
                            ActionUpdateSelectPaymentMethodParams(
                                paymentMethodType = PaymentMethodType.KLARNA.name,
                                cardNetwork = null
                            )
                        )
                    )
                ).getOrThrow().clientSession.fees.orEmpty()
        }

    private data class Optional(val value: Int?)
}
