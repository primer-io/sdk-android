package io.primer.android.internal.domain.repositories

import io.primer.android.internal.domain.Cleanable

internal interface NativeUiRepository : Cleanable {
    fun startPaymentFlow(paymentMethodType: String)
}
