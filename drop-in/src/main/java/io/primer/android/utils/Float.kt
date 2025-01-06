package io.primer.android.utils

import android.content.Context
import android.util.TypedValue

internal fun Float.dPtoPx(context: Context) =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        context.resources.displayMetrics,
    )
