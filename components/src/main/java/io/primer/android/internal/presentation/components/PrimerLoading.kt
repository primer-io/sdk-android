package io.primer.android.internal.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.primer.android.LocalPrimerTheme

@Composable
internal fun PrimerLoading(
    size: Dp = LocalPrimerTheme.current.sizeTokens.xxxlarge,
    padding: Dp = LocalPrimerTheme.current.spacingTokens.xsmall,
    strokeWidth: Dp = LocalPrimerTheme.current.spacingTokens.xsmall,
) {
    Box(
        modifier = Modifier
            .size(size)
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            strokeWidth = strokeWidth,
        )
    }
}
