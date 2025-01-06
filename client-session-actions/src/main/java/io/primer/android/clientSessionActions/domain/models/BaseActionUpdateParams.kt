package io.primer.android.clientSessionActions.domain.models

import io.primer.android.core.domain.Params

sealed interface BaseActionUpdateParams : Params

data class MultipleActionUpdateParams(
    val params: List<BaseActionUpdateParams>,
) : Params
