package io.primer.composable.internal.presentation.screens.success

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.primer.composable.R
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSizeTokens

@Composable
internal fun DefaultSuccessScreen(
    modifier: Modifier = Modifier,
) {
    val sizes = LocalPrimerSizeTokens.current
    val colorTokens = LocalPrimerColorTokens.current

    Column(
        modifier = modifier
            .padding(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_primer_success),
            contentDescription = "Payment successful",
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(sizes.xxxlarge))

        Text(
            text = "Payment successful",
            style = MaterialTheme.typography.headlineSmall,
            color = colorTokens.primerColorTextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(sizes.xxxlarge))

        Text(
            text = "You'll be redirected to the order confirmation page soon.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorTokens.primerColorTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
