package io.primer.android.domain.action.models

import io.primer.android.domain.base.Params

internal sealed interface BaseActionUpdateParams : Params

internal data class MultipleActionUpdateParams(
    val params: List<BaseActionUpdateParams>
) : Params
