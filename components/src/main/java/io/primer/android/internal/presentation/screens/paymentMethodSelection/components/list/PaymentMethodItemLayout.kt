package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.ui.assets.PrimerIconPosition

/**
 * A slot-based layout for payment method items that handles icon positioning.
 *
 * @param iconPosition Where to place the icon relative to text
 * @param icon The icon content slot (nullable)
 * @param text The text content slot (nullable)
 */
@Composable
internal fun PaymentMethodItemLayout(
    iconPosition: PrimerIconPosition,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
) {
    val layoutDirection = LocalLayoutDirection.current
    val spacing = LocalPrimerTheme.current.spacingTokens.small

    when (iconPosition) {
        PrimerIconPosition.START, PrimerIconPosition.END -> {
            HorizontalLayout(
                iconPosition = iconPosition,
                layoutDirection = layoutDirection,
                icon = icon,
                text = text,
                modifier = modifier,
            )
        }
        PrimerIconPosition.ABOVE -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier,
            ) {
                icon?.invoke()
                if (icon != null && text != null) {
                    Column(modifier = Modifier.padding(top = spacing)) {
                        text.invoke()
                    }
                } else {
                    text?.invoke()
                }
            }
        }
        PrimerIconPosition.BELOW -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier,
            ) {
                text?.invoke()
                if (icon != null && text != null) {
                    Column(modifier = Modifier.padding(top = spacing)) {
                        icon.invoke()
                    }
                } else {
                    icon?.invoke()
                }
            }
        }
    }
}

@Composable
private fun HorizontalLayout(
    iconPosition: PrimerIconPosition,
    layoutDirection: LayoutDirection,
    icon: (@Composable () -> Unit)?,
    text: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalPrimerTheme.current.spacingTokens.small

    val showIconFirst = (iconPosition == PrimerIconPosition.START && layoutDirection == LayoutDirection.Ltr) ||
        (iconPosition == PrimerIconPosition.END && layoutDirection == LayoutDirection.Rtl)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (layoutDirection == LayoutDirection.Rtl) {
            Arrangement.End
        } else {
            Arrangement.Start
        },
        modifier = modifier,
    ) {
        if (showIconFirst) {
            icon?.invoke()
            if (icon != null && text != null) {
                Row(modifier = Modifier.padding(start = spacing)) {
                    text.invoke()
                }
            } else {
                text?.invoke()
            }
        } else {
            text?.invoke()
            if (icon != null && text != null) {
                Row(modifier = Modifier.padding(start = spacing)) {
                    icon.invoke()
                }
            } else {
                icon?.invoke()
            }
        }
    }
}
