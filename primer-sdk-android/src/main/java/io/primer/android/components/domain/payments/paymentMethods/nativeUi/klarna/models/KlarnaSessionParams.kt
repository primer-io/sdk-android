package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.PrimerSessionIntent
import io.primer.android.domain.base.Params

internal data class KlarnaSessionParams(
    val surcharge: Int?,
    val primerSessionIntent: PrimerSessionIntent
) : Params
