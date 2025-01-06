package io.primer.android.paypal.implementation.tokenization.domain.repository

import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCheckoutConfiguration
import kotlinx.coroutines.flow.Flow

internal interface PaypalCheckoutConfigurationRepository {
    fun getPaypalConfiguration(): Flow<PaypalCheckoutConfiguration>
}
