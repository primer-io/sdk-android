package io.primer.android.components.domain.exception

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory

class UnsupportedPaymentMethodManagerException(
    paymentMethodType: String,
    category: PrimerPaymentMethodManagerCategory,
) : IllegalStateException("Payment method $paymentMethodType is not supported on $category manager")
