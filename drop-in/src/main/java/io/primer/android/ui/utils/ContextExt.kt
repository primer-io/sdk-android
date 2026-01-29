package io.primer.android.ui.utils

import android.content.Context
import android.util.TypedValue

internal fun Context.toPx(dp: Int): Float =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics,
    )
