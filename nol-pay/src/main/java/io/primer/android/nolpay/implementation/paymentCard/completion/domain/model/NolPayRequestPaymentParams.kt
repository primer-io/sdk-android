package io.primer.android.nolpay.implementation.paymentCard.completion.domain.model

import android.nfc.Tag
import io.primer.android.core.domain.Params

internal data class NolPayRequestPaymentParams(val tag: Tag, val transactionNo: String) : Params
