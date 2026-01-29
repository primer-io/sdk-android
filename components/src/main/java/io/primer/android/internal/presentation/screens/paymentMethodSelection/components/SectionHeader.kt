package io.primer.android.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
internal fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val theme = LocalPrimerTheme.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = theme.typographyTokens.titleLarge.toTextStyle(),
            color = theme.colorTokens().primerColorTextPrimary,
        )

        trailingContent?.invoke()
    }
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun SectionHeaderPreview() = PreviewContainer {
    SectionHeader(title = "Payment Methods")
}
