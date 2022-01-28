package io.primer.android.ui.extensions

import androidx.fragment.app.Fragment
import io.primer.android.ui.utils.AutoClearedValue

internal fun <T : Any> Fragment.autoCleaned(initializer: (() -> T)? = null): AutoClearedValue<T> {
    return AutoClearedValue(this, initializer)
}
