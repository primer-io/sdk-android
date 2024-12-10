package io.primer.android.nolpay.implementation.common.domain.model

import android.nfc.Tag
import io.primer.android.core.domain.Params

internal data class NolPayTagParams(val tag: Tag) : Params
