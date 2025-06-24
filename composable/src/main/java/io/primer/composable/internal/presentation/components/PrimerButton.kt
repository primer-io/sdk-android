package io.primer.composable.internal.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerRadiusTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSizeTokens

@Composable
fun PrimerButton(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    borderRadius: Dp = LocalPrimerRadiusTokens.current.medium,
    backgroundColor: Color = LocalPrimerColorTokens.current.primerColorBackground,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorTokens = LocalPrimerColorTokens.current
    val effectiveBackgroundColor = if (enabled) backgroundColor else colorTokens.primerColorGray200
    val effectiveBorderColor = if (enabled) borderColor else colorTokens.primerColorBorderOutlinedDisabled

    // TODO COMPOSABLE change to regular Button
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(LocalPrimerSizeTokens.current.xxlarge)
            .background(
                color = effectiveBackgroundColor,
                shape = RoundedCornerShape(borderRadius),
            )
            .then(
                effectiveBorderColor?.let {
                    Modifier.border(
                        width = 1.dp,
                        color = it,
                        shape = RoundedCornerShape(borderRadius),
                    )
                } ?: Modifier,
            )
            .then(
                if (enabled) {
                    Modifier.clickable { onClick.invoke() }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) { content() }
}
