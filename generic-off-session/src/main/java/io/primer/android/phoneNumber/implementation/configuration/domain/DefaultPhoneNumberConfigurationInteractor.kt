package io.primer.android.phoneNumber.implementation.configuration.domain

import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.phoneNumber.implementation.configuration.domain.model.PhoneNumberConfig
import io.primer.android.phoneNumber.implementation.configuration.domain.model.PhoneNumberConfigParams

internal typealias PhoneNumberConfigurationInteractor =
    PaymentMethodConfigurationInteractor<PhoneNumberConfig, PhoneNumberConfigParams>

internal class DefaultPhoneNumberConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<PhoneNumberConfig, PhoneNumberConfigParams>,
) : PaymentMethodConfigurationInteractor<PhoneNumberConfig, PhoneNumberConfigParams>(
    configurationRepository,
)
