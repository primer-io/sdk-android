package io.primer.android.domain.payments.displayMetadata.repository

import io.primer.android.domain.payments.displayMetadata.models.PaymentMethodImplementation

internal interface PaymentMethodImplementationRepository {

    fun getPaymentMethodsImplementation(): List<PaymentMethodImplementation>
}
