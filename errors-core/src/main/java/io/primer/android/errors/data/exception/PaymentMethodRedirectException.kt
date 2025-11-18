package io.primer.android.errors.data.exception

import kotlin.coroutines.cancellation.CancellationException

data class PaymentMethodRedirectException(val paymentMethodType: String, val uri: String) :
    CancellationException()
