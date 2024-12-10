package io.primer.android.stripe.ach.implementation.tokenization.presentation

import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.core.extensions.flatMap
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.android.stripe.ach.implementation.configuration.domain.StripeAchConfigurationInteractor
import io.primer.android.stripe.ach.implementation.configuration.domain.model.StripeAchConfigParams
import io.primer.android.stripe.ach.implementation.tokenization.domain.StripeAchTokenizationInteractor
import io.primer.android.stripe.ach.implementation.tokenization.domain.model.StripeAchPaymentInstrumentParams
import io.primer.android.stripe.ach.implementation.tokenization.presentation.model.StripeAchTokenizationInputable

internal class StripeAchTokenizationDelegate(
    private val stripeAchConfigurationInteractor: StripeAchConfigurationInteractor,
    private val primerSettings: PrimerSettings,
    tokenizationInteractor: StripeAchTokenizationInteractor,
    private val actionInteractor: ActionInteractor
) : PaymentMethodTokenizationDelegate<StripeAchTokenizationInputable, StripeAchPaymentInstrumentParams>(
    tokenizationInteractor
),
    TokenizationCollectedDataMapper<StripeAchTokenizationInputable, StripeAchPaymentInstrumentParams> {
    override suspend fun mapTokenizationData(input: StripeAchTokenizationInputable) =
        if (primerSettings.sdkIntegrationType == SdkIntegrationType.HEADLESS) {
            updateSelectedPaymentMethodParams(paymentMethodType = input.paymentMethodType)
        } else {
            Result.success(Unit)
        }
            .flatMap {
                stripeAchConfigurationInteractor(StripeAchConfigParams(paymentMethodType = input.paymentMethodType))
            }.map { configuration ->
                TokenizationParams(
                    paymentInstrumentParams = StripeAchPaymentInstrumentParams(
                        paymentMethodConfigId = configuration.paymentMethodConfigId,
                        locale = configuration.locale.toLanguageTag()
                    ),
                    sessionIntent = input.primerSessionIntent
                )
            }

    private suspend fun updateSelectedPaymentMethodParams(paymentMethodType: String) = actionInteractor(
        MultipleActionUpdateParams(
            listOf(
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = paymentMethodType,
                    cardNetwork = null
                )
            )
        )
    )
}
