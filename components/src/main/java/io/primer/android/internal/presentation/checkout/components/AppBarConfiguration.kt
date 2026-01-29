@file:Suppress("TooManyFunctions")

package io.primer.android.internal.presentation.checkout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.data.settings.DismissalMechanism
import io.primer.android.internal.presentation.checkout.navigation.LocalSheetNavController

/**
 * Indicates whether we're in inline flow (PrimerCheckoutHost) vs modal flow (PrimerCheckoutSheet).
 * In inline flow, the close button should always be shown on overlay screens.
 */
internal val LocalIsInlineFlow = staticCompositionLocalOf { false }

/**
 * Wrapper that provides app bar spec and renders the app bar above content.
 *
 * Usage in NavGraph:
 * ```
 * composable<Screen.Klarna> {
 *     WithAppBar(
 *         AppBarSpec.Standard(
 *             title = stringResource(R.string.primer_klarna_title),
 *             titleAlignment = TitleAlignment.Center,
 *             actions = Actions.Close,
 *         )
 *     ) {
 *         KlarnaScreen(checkout)
 *     }
 * }
 * ```
 */
@Composable
internal fun WithAppBar(
    spec: AppBarSpec,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppBarSpec provides spec) {
        Column(modifier = Modifier.fillMaxWidth()) {
            DefaultAppBar()
            content()
        }
    }
}

/**
 * Renders the app bar based on the current [AppBarSpec] from [LocalAppBarSpec].
 *
 * This is a resolver - it takes the declarative spec and resolves actual
 * behavior based on navigation state, settings, and inline flow.
 */
@Composable
internal fun DefaultAppBar() {
    val spec = LocalAppBarSpec.current
    if (spec is AppBarSpec.Hidden) return

    val standard = spec as AppBarSpec.Standard
    val resolved = resolveAppBarState(standard)
    val hasTitle = !standard.title.isNullOrEmpty()

    // Show app bar if there's title, back button, or trailing action
    if (!hasTitle && !resolved.hasContent) return

    RenderAppBar(
        title = standard.title,
        titleAlignment = standard.titleAlignment,
        showBack = resolved.showBack,
        trailingAction = resolved.trailingAction,
    )
}

private data class ResolvedAppBarState(
    val showBack: Boolean,
    val trailingAction: TrailingAction,
) {
    val hasContent: Boolean
        get() = showBack || trailingAction != TrailingAction.None
}

private sealed interface TrailingAction {
    data object None : TrailingAction
    data object Close : TrailingAction
    data class Custom(val content: @Composable () -> Unit) : TrailingAction
}

@Composable
private fun resolveAppBarState(spec: AppBarSpec.Standard): ResolvedAppBarState {
    val navController = LocalSheetNavController.current
    val canGoBack = navController.previousBackStackEntry != null

    // Both Back and Auto check canGoBack - Back is explicit intent, Auto is default
    val showBack = when (spec.navigation) {
        Navigation.None -> false
        Navigation.Back -> canGoBack
        Navigation.Auto -> canGoBack
    }

    val trailingAction = resolveActions(spec.actions)

    return ResolvedAppBarState(showBack, trailingAction)
}

@Composable
private fun resolveActions(actions: Actions): TrailingAction {
    return when (actions) {
        Actions.None -> TrailingAction.None
        Actions.Close -> TrailingAction.Close
        is Actions.Custom -> TrailingAction.Custom(actions.content)
        Actions.Auto -> {
            val settings = runCatching { LocalPrimerSettings.current }.getOrNull()
            val settingsAllow = settings?.let {
                DismissalMechanism.CLOSE_BUTTON in it.uiOptions.dismissalMechanism
            } ?: false
            val isInlineFlow = LocalIsInlineFlow.current

            if (settingsAllow || isInlineFlow) {
                TrailingAction.Close
            } else {
                TrailingAction.None
            }
        }
    }
}

@Composable
private fun RenderAppBar(
    title: String?,
    titleAlignment: TitleAlignment,
    showBack: Boolean,
    trailingAction: TrailingAction,
) {
    val spacing = LocalPrimerTheme.current.spacingTokens
    val hasTitle = !title.isNullOrEmpty()
    val hasTrailingAction = trailingAction != TrailingAction.None

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.small),
        ) {
            if (hasTitle && titleAlignment == TitleAlignment.Center) {
                CenteredTitle(title = title, modifier = Modifier.align(Alignment.Center))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    showBack -> BackButton()
                    hasTitle && titleAlignment == TitleAlignment.Start -> {
                        InlineTitle(title = title, hasTrailingAction = hasTrailingAction)
                    }
                    else -> Box(modifier = Modifier) // Spacer for layout
                }

                // Trailing action
                TrailingSlot(trailingAction)
            }
        }

        if (hasTitle && titleAlignment == TitleAlignment.Start && showBack) {
            BelowBackTitle(title = title)
        }
    }
}

@Composable
private fun BackButton() {
    val theme = LocalPrimerTheme.current
    val navController = LocalSheetNavController.current
    val backIconRes = if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
        R.drawable.ic_primer_chevron_right
    } else {
        R.drawable.ic_primer_chevron_left
    }

    TextButton(onClick = { navController.popBackStack() }) {
        Icon(
            painter = painterResource(id = backIconRes),
            contentDescription = stringResource(R.string.primer_common_back),
            tint = theme.colorTokens().primerColorTextPrimary,
            modifier = Modifier.size(theme.sizeTokens.medium),
        )
        Text(
            text = stringResource(R.string.primer_common_back),
            style = theme.typographyTokens.titleLarge.toTextStyle(),
            color = theme.colorTokens().primerColorTextPrimary,
        )
    }
}

@Composable
private fun InlineTitle(title: String, hasTrailingAction: Boolean) {
    val theme = LocalPrimerTheme.current
    val spacing = theme.spacingTokens
    val modifier = if (hasTrailingAction) {
        Modifier.padding(start = spacing.large)
    } else {
        Modifier.padding(horizontal = spacing.large, vertical = spacing.medium)
    }

    Text(
        text = title,
        style = theme.typographyTokens.titleXlarge.toTextStyle(),
        color = theme.colorTokens().primerColorTextPrimary,
        modifier = modifier,
    )
}

@Composable
private fun CenteredTitle(title: String, modifier: Modifier = Modifier) {
    val theme = LocalPrimerTheme.current
    Text(
        text = title,
        style = theme.typographyTokens.titleLarge.toTextStyle(),
        color = theme.colorTokens().primerColorTextPrimary,
        modifier = modifier,
    )
}

@Composable
private fun BelowBackTitle(title: String) {
    val theme = LocalPrimerTheme.current
    val spacing = theme.spacingTokens
    Text(
        text = title,
        style = theme.typographyTokens.titleXlarge.toTextStyle(),
        color = theme.colorTokens().primerColorTextPrimary,
        modifier = Modifier.padding(horizontal = spacing.large),
    )
}

@Composable
private fun TrailingSlot(action: TrailingAction) {
    when (action) {
        TrailingAction.None -> { /* Nothing */ }
        TrailingAction.Close -> CloseButton()
        is TrailingAction.Custom -> action.content()
    }
}

@Composable
private fun CloseButton() {
    val theme = LocalPrimerTheme.current
    val dismissAction = LocalDismissAction.current

    TextButton(onClick = dismissAction) {
        Text(
            text = stringResource(R.string.primer_common_button_cancel),
            style = theme.typographyTokens.titleLarge.toTextStyle(),
            color = theme.colorTokens().primerColorTextPrimary,
        )
    }
}
