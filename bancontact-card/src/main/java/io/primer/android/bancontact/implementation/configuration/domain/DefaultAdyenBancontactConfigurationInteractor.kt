package io.primer.android.bancontact.implementation.configuration.domain

import io.primer.android.bancontact.implementation.configuration.domain.model.AdyenBancontactConfig
import io.primer.android.bancontact.implementation.configuration.domain.model.AdyenBancontactConfigParams
import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository

internal typealias AdyenBancontactConfigurationInteractor =
    PaymentMethodConfigurationInteractor<AdyenBancontactConfig, AdyenBancontactConfigParams>

internal class DefaultAydenBancontactConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<AdyenBancontactConfig, AdyenBancontactConfigParams>,
) : PaymentMethodConfigurationInteractor<AdyenBancontactConfig, AdyenBancontactConfigParams>(
    configurationRepository,
)
