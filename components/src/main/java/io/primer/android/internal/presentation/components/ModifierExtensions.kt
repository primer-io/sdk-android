package io.primer.android.internal.presentation.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

/**
 * Conditionally applies a content description for accessibility if not null.
 */
internal fun Modifier.contentDescriptionIfNotNull(description: String?): Modifier =
    if (description != null) semantics { contentDescription = description } else this

/**
 * Conditionally applies both content and state descriptions for accessibility.
 */
internal fun Modifier.accessibilityDescriptions(
    contentDesc: String?,
    stateDesc: String?,
): Modifier = when {
    contentDesc != null && stateDesc != null -> semantics {
        contentDescription = contentDesc
        stateDescription = stateDesc
    }
    contentDesc != null -> semantics { contentDescription = contentDesc }
    stateDesc != null -> semantics { stateDescription = stateDesc }
    else -> this
}

/**
 * Conditionally applies a focus change listener if the callback is not null.
 */
internal fun Modifier.onFocusChangedIfNotNull(callback: ((Boolean) -> Unit)?): Modifier =
    if (callback != null) onFocusChanged { callback(it.isFocused) } else this

/**
 * Applies a polite live region with optional content description.
 * Used for loading indicators and dynamic content announcements.
 */
internal fun Modifier.liveRegionPolite(description: String? = null): Modifier =
    semantics {
        liveRegion = LiveRegionMode.Polite
        description?.let { contentDescription = it }
    }
