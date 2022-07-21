package io.primer.android.ui.extensions

import android.content.ClipData
import android.content.ClipboardManager
import androidx.core.content.getSystemService
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

internal fun Fragment.copyTextToClipboard(text: String): Boolean {
    val clipboardManager = (requireActivity().getSystemService<ClipboardManager>())
    clipboardManager?.apply {
        setPrimaryClip(ClipData.newPlainText("Primer SDK", text))
    }
    return clipboardManager != null
}
