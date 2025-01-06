package io.primer.android.ui.extensions

import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import androidx.annotation.DimenRes

private const val DISPLAY_RATIO = 9 / 16

internal fun Context.getScreenHeight() =
    getDisplaySize().y - getStatusBarHeight()

internal fun Context.getCollapsedSheetHeight() = getScreenHeight() -
    getDisplaySize().x * DISPLAY_RATIO

private fun Context.getStatusBarHeight(): Int = resources
    .getIdentifier("status_bar_height", "dimen", "android")
    .takeIf { resourceId -> resourceId > 0 }
    ?.let { resourceId -> resources.getDimensionPixelSize(resourceId) }
    ?: 0

private fun Context.getDisplaySize() =
    Point().also { point ->
        getSystemService(Context.WINDOW_SERVICE)
            .let { it as WindowManager }
            .defaultDisplay
            .getSize(point)
    }

internal fun Context.getDimensionAsPx(@DimenRes dimenResId: Int): Int {
    val dpi = resources.displayMetrics.densityDpi
    val dp = resources.getDimension(dimenResId)
    val px = dp * (dpi / 160)
    return px.toInt()
}
