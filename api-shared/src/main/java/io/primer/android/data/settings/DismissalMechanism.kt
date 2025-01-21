package io.primer.android.data.settings

import io.primer.android.data.settings.DismissalMechanism.CLOSE_BUTTON
import io.primer.android.data.settings.DismissalMechanism.GESTURES

/**
 * Enum class representing the mechanisms by which a Checkout can be dismissed.
 * [GESTURES]: Dismissal occurs via user gestures, such as swiping down.
 * [CLOSE_BUTTON]: Dismissal occurs when the close button is explicitly tapped by the user.
 */
enum class DismissalMechanism {
    GESTURES,
    CLOSE_BUTTON,
}
