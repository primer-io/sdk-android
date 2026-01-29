package io.primer.android.internal.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
internal fun PrimerLoading(
    size: Dp = LocalPrimerTheme.current.sizeTokens.xxxlarge,
    padding: Dp = LocalPrimerTheme.current.spacingTokens.xsmall,
    strokeWidth: Dp = LocalPrimerTheme.current.spacingTokens.xsmall,
    accessibilityLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .padding(padding)
            .liveRegionPolite(accessibilityLabel),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            strokeWidth = strokeWidth,
        )
    }
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun PrimerLoadingPreview() = PreviewContainer {
    PrimerLoading()
}
