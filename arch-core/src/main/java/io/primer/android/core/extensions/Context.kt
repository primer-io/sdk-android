package io.primer.android.core.extensions

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.isNightModeEnabled(): Boolean {
    val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return currentNightMode == Configuration.UI_MODE_NIGHT_YES
}
