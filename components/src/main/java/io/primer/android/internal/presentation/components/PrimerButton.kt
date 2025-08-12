package io.primer.android.internal.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme

@Suppress("LongParameterList")
@Composable
fun PrimerButton(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    borderRadius: Dp = LocalPrimerTheme.current.radiusTokens.medium,
    backgroundColor: Color = LocalPrimerTheme.current.colorTokens().primerColorBackground,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(LocalPrimerTheme.current.sizeTokens.xxlarge),
        enabled = enabled,
        shape = RoundedCornerShape(borderRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
        ),
        border = borderColor?.let {
            BorderStroke(width = 1.dp, color = it)
        },
    ) {
        content()
    }
}
