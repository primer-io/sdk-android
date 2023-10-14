package io.primer.android.components.domain.payments.paymentMethods.nolpay.models

import android.nfc.Tag
import io.primer.android.domain.base.Params

internal data class NolPayRequestPaymentParams(val tag: Tag, val transactionNo: String) : Params
