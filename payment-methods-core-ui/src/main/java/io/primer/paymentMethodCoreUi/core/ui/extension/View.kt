package io.primer.paymentMethodCoreUi.core.ui.extension

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Applies window inset paddings (e.g. system bars, cutouts, IME) to the [View]'s padding.
 *
 * This sets an [androidx.core.view.OnApplyWindowInsetsListener] on the view and updates its padding
 * dynamically to reflect the provided inset types.
 *
 * This is useful when using edge-to-edge mode and needing to ensure content
 * is not obscured by system UI elements like gesture bars or the keyboard.
 *
 * @param insetTypes Bitmask of inset types to apply. Defaults to system bars, cutout, and IME.
 *                   Use [WindowInsetsCompat.Type.systemBars], [WindowInsetsCompat.Type.ime], etc.
 *
 * Example:
 * ```
 * // Apply only navigation bar and IME insets
 * view.applyFullWindowInsetsPadding(
 *     WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.ime()
 * )
 * ```
 */
fun View.applyFullWindowInsetsPadding(
    insetTypes: Int = WindowInsetsCompat.Type.systemBars() or
        WindowInsetsCompat.Type.displayCutout() or
        WindowInsetsCompat.Type.ime(),
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(insetTypes)
        view.updatePadding(
            left = bars.left,
            top = bars.top,
            right = bars.right,
            bottom = bars.bottom,
        )
        WindowInsetsCompat.CONSUMED
    }
}
