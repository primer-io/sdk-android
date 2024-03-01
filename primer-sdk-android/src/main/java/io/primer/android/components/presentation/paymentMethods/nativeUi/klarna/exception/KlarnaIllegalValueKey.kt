package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class KlarnaIllegalValueKey(override val key: String) : IllegalValueKey {
    KLARNA_SESSION("klarnaSession"),
    KLARNA_PAYMENT_VIEW("klarnaPaymentView")
}
