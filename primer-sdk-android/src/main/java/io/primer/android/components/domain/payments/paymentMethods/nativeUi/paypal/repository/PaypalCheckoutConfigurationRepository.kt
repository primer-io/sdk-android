package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCheckoutConfiguration
import kotlinx.coroutines.flow.Flow

internal interface PaypalCheckoutConfigurationRepository {

    fun getPaypalConfiguration(): Flow<PaypalCheckoutConfiguration>
}
