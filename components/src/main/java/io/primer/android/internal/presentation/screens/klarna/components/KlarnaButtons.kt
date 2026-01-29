package io.primer.android.internal.presentation.screens.klarna.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R

/**
 * Authorize button to continue with Klarna payment.
 */
@Composable
internal fun KlarnaAuthorizeButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = LocalPrimerTheme.current
    val colorTokens = theme.colorTokens()

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = theme.spacingTokens.large),
        shape = RoundedCornerShape(theme.radiusTokens.medium),
        colors = ButtonDefaults.buttonColors(containerColor = colorTokens.primerColorBrand),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(theme.sizeTokens.medium),
                color = Color.White,
                strokeWidth = theme.borderWidthTokens.medium,
            )
        } else {
            Text(
                text = stringResource(R.string.primer_klarna_button_authorize),
                style = theme.typographyTokens.titleLarge.toTextStyle(),
            )
        }
    }
}

/**
 * Finalize button to complete Klarna payment (shown if additional step needed).
 */
@Composable
internal fun KlarnaFinalizeButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = LocalPrimerTheme.current
    val colorTokens = theme.colorTokens()

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = theme.spacingTokens.large),
        shape = RoundedCornerShape(theme.radiusTokens.medium),
        colors = ButtonDefaults.buttonColors(containerColor = colorTokens.primerColorBrand),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(theme.sizeTokens.medium),
                color = Color.White,
                strokeWidth = theme.borderWidthTokens.medium,
            )
        } else {
            Text(
                text = stringResource(R.string.primer_klarna_button_finalize),
                style = theme.typographyTokens.titleLarge.toTextStyle(),
            )
        }
    }
}
