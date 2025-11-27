package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.primer.android.LocalPrimerTheme

/**
 * Shared gray background container used to highlight vaulted payment method actions.
 */
@Composable
internal fun VaultedHighlightedContainer(
    modifier: Modifier = Modifier,
    contentSpacing: Dp = LocalPrimerTheme.current.spacingTokens.medium,
    content: @Composable ColumnScope.() -> Unit,
) {
    val theme = LocalPrimerTheme.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = theme.colorTokens().primerColorGray100,
                shape = RoundedCornerShape(theme.radiusTokens.medium),
            )
            .padding(theme.spacingTokens.small),
        verticalArrangement = Arrangement.spacedBy(contentSpacing),
        content = content,
    )
}
