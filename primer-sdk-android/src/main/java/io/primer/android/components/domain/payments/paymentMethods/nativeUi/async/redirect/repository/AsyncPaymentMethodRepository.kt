package io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.repository

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.models.AsyncPaymentMethodConfig
import kotlinx.coroutines.flow.Flow

internal interface AsyncPaymentMethodRepository {

    fun getPaymentMethodConfig(paymentMethodType: String): Flow<AsyncPaymentMethodConfig>
}
