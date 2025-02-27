package io.primer.android.ui.extensions

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

const val NO_COLOR = -1

internal fun Drawable.updateTint(
    @ColorInt color: Int,
): Drawable {
    if (color >= 0) {
        DrawableCompat.setTint(this, color)
        DrawableCompat.setTintMode(this, PorterDuff.Mode.SRC_IN)
    }
    return this
}
