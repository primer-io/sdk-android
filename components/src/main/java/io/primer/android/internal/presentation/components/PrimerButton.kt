package io.primer.android.internal.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
fun PrimerButton(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    borderRadius: Dp = LocalPrimerTheme.current.radiusTokens.medium,
    backgroundColor: Color = LocalPrimerTheme.current.colorTokens().primerColorBackground,
    onClick: () -> Unit,
    enabled: Boolean = true,
    accessibilityLabel: String? = null,
    accessibilityStateDescription: String? = null,
    content: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = LocalPrimerTheme.current.sizeTokens.xxlarge)
            .accessibilityDescriptions(accessibilityLabel, accessibilityStateDescription),
        enabled = enabled,
        shape = RoundedCornerShape(borderRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
        ),
        border = borderColor?.let {
            BorderStroke(width = LocalPrimerTheme.current.borderWidthTokens.thin, color = it)
        },
    ) {
        content()
    }
}

@Preview(name = "Primary", showBackground = true)
@Composable
private fun PrimerButtonPrimaryPreview() = PreviewContainer {
    PrimerButton(onClick = {}) { Text("Pay Now") }
}

@Preview(name = "Outlined", showBackground = true)
@Composable
private fun PrimerButtonOutlinedPreview() = PreviewContainer {
    PrimerButton(
        onClick = {},
        borderColor = LocalPrimerTheme.current.colorTokens().primerColorBorderOutlinedDefault,
    ) { Text("Cancel") }
}

@Preview(name = "Disabled", showBackground = true)
@Composable
private fun PrimerButtonDisabledPreview() = PreviewContainer {
    PrimerButton(onClick = {}, enabled = false) { Text("Disabled") }
}
