package io.primer.android.paymentmethods.core.configuration.domain.repository

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration
import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

interface PaymentMethodConfigurationRepository<T : PaymentMethodConfiguration, U : PaymentMethodConfigurationParams> {

    fun getPaymentMethodConfiguration(params: U): Result<T>
}
