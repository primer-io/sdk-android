package io.primer.android.components.assets.extensions

import android.util.DisplayMetrics

internal const val LDPI_SCALE = 0.75f
internal const val MDPI_SCALE = 1f
internal const val HDPI_SCALE = 1.5f
internal const val XHDPI_SCALE = 2f
internal const val XXHDPI_SCALE = 3f
internal const val XXXHDPI_SCALE = 4f

internal fun DisplayMetrics.toResourcesScale() =
    when (densityDpi) {
        DisplayMetrics.DENSITY_LOW -> LDPI_SCALE
        DisplayMetrics.DENSITY_MEDIUM -> MDPI_SCALE
        DisplayMetrics.DENSITY_HIGH -> HDPI_SCALE
        DisplayMetrics.DENSITY_XHIGH, DisplayMetrics.DENSITY_280 -> XHDPI_SCALE
        DisplayMetrics.DENSITY_XXHIGH, DisplayMetrics.DENSITY_360, DisplayMetrics.DENSITY_400,
        DisplayMetrics.DENSITY_420, DisplayMetrics.DENSITY_440, DisplayMetrics.DENSITY_450,
        ->
            XXHDPI_SCALE

        DisplayMetrics.DENSITY_XXXHIGH, DisplayMetrics.DENSITY_560 -> XXXHDPI_SCALE
        else -> density
    }
