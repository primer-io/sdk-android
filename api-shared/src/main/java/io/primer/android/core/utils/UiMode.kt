package io.primer.android.core.utils

import android.content.Context
import android.content.res.Configuration

internal class UiMode private constructor() {
    companion object {
        fun useDarkTheme(context: Context): Boolean {
            val uiMode = context.resources.configuration.uiMode

            return when (uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                Configuration.UI_MODE_NIGHT_UNDEFINED -> false
                else -> false
            }
        }
    }
}
