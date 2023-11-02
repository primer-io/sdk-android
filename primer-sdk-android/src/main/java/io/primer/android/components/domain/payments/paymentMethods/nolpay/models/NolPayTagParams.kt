package io.primer.android.components.domain.payments.paymentMethods.nolpay.models

import android.nfc.Tag
import io.primer.android.domain.base.Params

internal data class NolPayTagParams(val tag: Tag) : Params
