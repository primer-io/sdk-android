package io.primer.android.ui.extensions

import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat

internal fun TextView.setCompoundDrawablesWithIntrinsicBoundsTinted(
    @DrawableRes left: Int,
    @DrawableRes top: Int,
    @DrawableRes right: Int,
    @DrawableRes bottom: Int,
    @ColorInt tintColor: Int,
    @ColorInt tintColorLeft: Int = tintColor,
    @ColorInt tintColorTop: Int = tintColor,
    @ColorInt tintColorRight: Int = tintColor,
    @ColorInt tintColorBottom: Int = tintColor
) {
    val drawableLeft = if (left != 0) {
        ResourcesCompat.getDrawable(
            resources,
            left,
            null
        )?.updateTint(tintColorLeft)
    } else { null }

    val drawableTop = if (top != 0) {
        ResourcesCompat.getDrawable(
            resources,
            top,
            null
        )?.updateTint(tintColorTop)
    } else { null }

    val drawableRight = if (right != 0) {
        ResourcesCompat.getDrawable(
            resources,
            right,
            null
        )?.updateTint(tintColorRight)
    } else { null }

    val drawableBottom = if (bottom != 0) {
        ResourcesCompat.getDrawable(
            resources,
            bottom,
            null
        )?.updateTint(tintColorBottom)
    } else { null }

    setCompoundDrawablesWithIntrinsicBounds(
        drawableLeft,
        drawableTop,
        drawableRight,
        drawableBottom
    )
}
