package io.primer.android.internal.domain.repositories

internal interface NativeUiRepository {
    fun startPaymentFlow(paymentMethodType: String)
    fun cleanup()
}
