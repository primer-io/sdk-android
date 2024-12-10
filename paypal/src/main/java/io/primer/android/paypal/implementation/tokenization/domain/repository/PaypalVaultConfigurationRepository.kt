package io.primer.android.paypal.implementation.tokenization.domain.repository

import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfig

internal fun interface PaypalVaultConfigurationRepository {

    suspend fun getPaypalConfiguration(): PaypalConfig.PaypalVaultConfiguration
}
