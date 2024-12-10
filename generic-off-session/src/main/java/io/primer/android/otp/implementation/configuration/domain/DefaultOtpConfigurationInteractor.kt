package io.primer.android.otp.implementation.configuration.domain

import io.primer.android.otp.implementation.configuration.domain.model.OtpConfig
import io.primer.android.otp.implementation.configuration.domain.model.OtpConfigParams
import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository

internal typealias OtpConfigurationInteractor =
    PaymentMethodConfigurationInteractor<OtpConfig, OtpConfigParams>

internal class DefaultOtpConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<OtpConfig, OtpConfigParams>
) : PaymentMethodConfigurationInteractor<OtpConfig, OtpConfigParams>(
    configurationRepository
)
