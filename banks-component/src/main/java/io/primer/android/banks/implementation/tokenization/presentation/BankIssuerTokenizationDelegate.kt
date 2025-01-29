package io.primer.android.banks.implementation.tokenization.presentation

import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.banks.implementation.configuration.domain.BankIssuerConfigurationInteractor
import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfigParams
import io.primer.android.banks.implementation.tokenization.domain.BankIssuerTokenizationInteractor
import io.primer.android.banks.implementation.tokenization.domain.model.BankIssuerPaymentInstrumentParams
import io.primer.android.banks.implementation.tokenization.presentation.model.BankIssuerTokenizationInputable
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.core.domain.None
import io.primer.android.core.extensions.flatMap
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.android.webRedirectShared.implementation.deeplink.domain.RedirectDeeplinkInteractor

internal class BankIssuerTokenizationDelegate(
    private val configurationInteractor: BankIssuerConfigurationInteractor,
    tokenizationInteractor: BankIssuerTokenizationInteractor,
    private val primerSettings: PrimerSettings,
    private val actionInteractor: ActionInteractor,
    private val deeplinkInteractor: RedirectDeeplinkInteractor,
) : PaymentMethodTokenizationDelegate<BankIssuerTokenizationInputable, BankIssuerPaymentInstrumentParams>(
    tokenizationInteractor,
),
    TokenizationCollectedDataMapper<BankIssuerTokenizationInputable, BankIssuerPaymentInstrumentParams> {
    override suspend fun mapTokenizationData(input: BankIssuerTokenizationInputable) =
        if (primerSettings.sdkIntegrationType == SdkIntegrationType.HEADLESS) {
            updateSelectedPaymentMethodParams(paymentMethodType = input.paymentMethodType)
        } else {
            Result.success(Unit)
        }.flatMap {
            configurationInteractor(BankIssuerConfigParams(paymentMethodType = input.paymentMethodType))
        }.map { configuration ->
            TokenizationParams(
                paymentInstrumentParams =
                BankIssuerPaymentInstrumentParams(
                    paymentMethodType = input.paymentMethodType,
                    paymentMethodConfigId = configuration.paymentMethodConfigId,
                    locale = configuration.locale.toLanguageTag(),
                    redirectionUrl = deeplinkInteractor(None),
                    bankIssuer = input.bankIssuer,
                ),
                sessionIntent = input.primerSessionIntent,
            )
        }

    private suspend fun updateSelectedPaymentMethodParams(paymentMethodType: String) =
        actionInteractor(
            MultipleActionUpdateParams(
                listOf(
                    ActionUpdateSelectPaymentMethodParams(
                        paymentMethodType = paymentMethodType,
                        cardNetwork = null,
                    ),
                ),
            ),
        )
}
