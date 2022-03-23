package io.primer.android.ui.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.primer.android.ui.utils.AutoClearedValue

internal fun <T : Any> Fragment.autoCleaned(initializer: (() -> T)? = null): AutoClearedValue<T> {
    return AutoClearedValue(this, initializer)
}

internal fun Fragment.popBackStackToRoot() = popBackStackToIndex(0)

private fun Fragment.popBackStackToIndex(index: Int) = childFragmentManager.apply {
    popBackStack(
        getBackStackEntryAt(index).id,
        FragmentManager.POP_BACK_STACK_INCLUSIVE
    )
}
