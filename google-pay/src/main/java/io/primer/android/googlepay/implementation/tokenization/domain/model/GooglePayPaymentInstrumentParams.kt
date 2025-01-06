package io.primer.android.googlepay.implementation.tokenization.domain.model

import com.google.android.gms.wallet.PaymentData
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams

internal class GooglePayPaymentInstrumentParams(
    override val paymentMethodType: String,
    val merchantId: String,
    val paymentData: PaymentData,
    val flow: GooglePayFlow,
) : BasePaymentInstrumentParams

internal enum class GooglePayFlow {
    GATEWAY,
}
