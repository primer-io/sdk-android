package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalVaultConfiguration
import kotlinx.coroutines.flow.Flow

internal interface PaypalVaultConfigurationRepository {

    fun getPaypalConfiguration(): Flow<PaypalVaultConfiguration>
}
