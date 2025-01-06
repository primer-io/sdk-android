package io.primer.android.ui.extensions

import android.content.ClipData
import android.content.ClipboardManager
import androidx.activity.ComponentDialog
import androidx.core.content.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.ui.utils.AutoClearedValue

internal fun <T : Any> Fragment.autoCleaned(initializer: (() -> T)? = null): AutoClearedValue<T> {
    return AutoClearedValue(this, initializer)
}

internal fun Fragment.popBackStackToRoot() =
    runCatching {
        popBackStackToIndex(index = 0)
    }.getOrElse {
        childFragmentManager.commit(allowStateLoss = true) {
            childFragmentManager.fragments.forEach(::remove)
        }
    }

private fun Fragment.popBackStackToIndex(index: Int) =
    childFragmentManager.apply {
        popBackStack(
            getBackStackEntryAt(index).id,
            FragmentManager.POP_BACK_STACK_INCLUSIVE,
        )
    }

internal fun Fragment.copyTextToClipboard(text: String): Boolean {
    val clipboardManager = (requireActivity().getSystemService<ClipboardManager>())
    clipboardManager?.apply {
        setPrimaryClip(ClipData.newPlainText("Primer SDK", text))
    }
    return clipboardManager != null
}

internal fun Fragment.getParentDialogOrNull() =
    ((parentFragment as? DialogFragment)?.dialog as? ComponentDialog).also {
        if (it == null) {
            DISdkContext.getContainerOrNull()?.resolve<LogReporter>()?.error("Error: expected ComponentDialog parent!")
        }
    }
