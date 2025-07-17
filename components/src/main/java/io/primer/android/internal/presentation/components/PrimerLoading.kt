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
import io.primer.android.internal.presentation.theme.LocalPrimerSizeTokens
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens

@Composable
internal fun PrimerLoading(
    size: Dp = LocalPrimerSizeTokens.current.xxxlarge,
    padding: Dp = LocalPrimerSpacingTokens.current.xsmall,
    strokeWidth: Dp = LocalPrimerSpacingTokens.current.xsmall
) {
    Box(
        modifier = Modifier
            .size(size)
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            strokeWidth = strokeWidth
        )
    }
}
