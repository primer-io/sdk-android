package io.primer.android.internal.presentation.checkout.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Declarative specification for app bar behavior.
 *
 * Screens declare their intent, the app bar resolves actual behavior
 * based on navigation state and settings.
 */
internal sealed interface AppBarSpec {

    /**
     * No app bar should be shown.
     */
    data object Hidden : AppBarSpec

    /**
     * Standard app bar with configurable title, navigation, and actions.
     *
     * @param title Optional title text. Null means no title.
     * @param titleAlignment How the title should be aligned.
     * @param navigation Navigation behavior (back button).
     * @param actions Trailing actions behavior (close button).
     */
    data class Standard(
        val title: String? = null,
        val titleAlignment: TitleAlignment = TitleAlignment.Start,
        val navigation: Navigation = Navigation.Auto,
        val actions: Actions = Actions.Auto,
    ) : AppBarSpec
}

/**
 * Title alignment options.
 */
internal enum class TitleAlignment {
    /** Title aligned to start (default). Shows below back button when back is visible. */
    Start,

    /** Title centered in the app bar. */
    Center,
}

/**
 * Navigation (back button) behavior.
 */
internal sealed interface Navigation {
    /** No back button. */
    data object None : Navigation

    /** Always show back button. */
    data object Back : Navigation

    /** Show back button if navigation stack has previous entry. */
    data object Auto : Navigation
}

/**
 * Trailing actions behavior.
 */
internal sealed interface Actions {
    /** No trailing actions. */
    data object None : Actions

    /** Always show close button. */
    data object Close : Actions

    /** Show close button based on settings (DismissalMechanism) or inline flow. */
    data object Auto : Actions

    /** Custom trailing content. */
    data class Custom(val content: @Composable () -> Unit) : Actions
}

/**
 * CompositionLocal for the current app bar specification.
 * Defaults to Hidden if not provided.
 */
internal val LocalAppBarSpec = staticCompositionLocalOf<AppBarSpec> {
    AppBarSpec.Hidden
}
