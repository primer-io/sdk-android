package io.primer.android.klarna.implementation.session.domain.models

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.domain.Params

internal data class KlarnaSessionParams(
    val surcharge: Int?,
    val primerSessionIntent: PrimerSessionIntent
) : Params
