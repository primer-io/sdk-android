package io.primer.android.components.assets.displayMetadata.repository

import io.primer.android.components.assets.displayMetadata.models.PaymentMethodImplementation

fun interface PaymentMethodImplementationRepository {
    fun getPaymentMethodsImplementation(): List<PaymentMethodImplementation>
}
