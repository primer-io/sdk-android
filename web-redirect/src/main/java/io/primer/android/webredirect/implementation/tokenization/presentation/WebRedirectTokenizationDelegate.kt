package io.primer.android.webredirect.implementation.tokenization.presentation

import io.primer.android.core.domain.None
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.android.webRedirectShared.implementation.deeplink.domain.RedirectDeeplinkInteractor
import io.primer.android.webredirect.implementation.configuration.domain.WebRedirectConfigurationInteractor
import io.primer.android.webredirect.implementation.configuration.domain.model.WebRedirectConfigParams
import io.primer.android.webredirect.implementation.tokenization.domain.WebRedirectTokenizationInteractor
import io.primer.android.webredirect.implementation.tokenization.domain.model.WebRedirectPaymentInstrumentParams
import io.primer.android.webredirect.implementation.tokenization.domain.platform.PlatformResolver
import io.primer.android.webredirect.implementation.tokenization.presentation.model.WebRedirectTokenizationInputable

internal class WebRedirectTokenizationDelegate(
    private val configurationInteractor: WebRedirectConfigurationInteractor,
    private val tokenizationInteractor: WebRedirectTokenizationInteractor,
    private val deeplinkInteractor: RedirectDeeplinkInteractor,
    private val platformResolver: PlatformResolver,
) : PaymentMethodTokenizationDelegate<WebRedirectTokenizationInputable, WebRedirectPaymentInstrumentParams>(
    tokenizationInteractor,
),
    TokenizationCollectedDataMapper<WebRedirectTokenizationInputable, WebRedirectPaymentInstrumentParams> {
    override suspend fun mapTokenizationData(input: WebRedirectTokenizationInputable) =
        configurationInteractor(WebRedirectConfigParams(paymentMethodType = input.paymentMethodType))
            .map { configuration ->
                TokenizationParams(
                    paymentInstrumentParams =
                    WebRedirectPaymentInstrumentParams(
                        paymentMethodType = input.paymentMethodType,
                        paymentMethodConfigId = configuration.paymentMethodConfigId,
                        locale = configuration.locale,
                        redirectionUrl = deeplinkInteractor(None),
                        platform = platformResolver.getPlatform(paymentMethodType = input.paymentMethodType),
                    ),
                    sessionIntent = input.primerSessionIntent,
                )
            }
}
