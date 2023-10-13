package io.primer.android.domain.tokenization.models.paymentInstruments.googlepay

import com.google.android.gms.wallet.PaymentData
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams

internal class GooglePayPaymentInstrumentParams(
    override val paymentMethodType: String,
    val merchantId: String,
    val paymentData: PaymentData,
    val flow: GooglePayFlow
) : BasePaymentInstrumentParams(paymentMethodType)

internal enum class GooglePayFlow {
    GATEWAY
}
