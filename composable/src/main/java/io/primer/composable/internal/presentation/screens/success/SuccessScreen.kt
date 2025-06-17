package io.primer.composable.internal.presentation.screens.success

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSizeTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.scope.PrimerCheckoutScope

@Composable
internal fun PrimerCheckoutScope.SuccessScreen(
    modifier: Modifier = Modifier,
) {
    val spacing = LocalPrimerSpacingTokens.current
    val sizeTokens = LocalPrimerSizeTokens.current
    val colorTokens = LocalPrimerColorTokens.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Success",
            tint = Color.White,
            modifier = Modifier.size(sizeTokens.xxxlarge)
        )
        
        Spacer(modifier = Modifier.height(spacing.xlarge))
        
        // Title
        Text(
            text = "Payment successful",
            style = MaterialTheme.typography.bodyLarge,
            color = colorTokens.primerColorTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(spacing.small))
        
        // Subtitle
        Text(
            text = "You'll be redirected to the order confirmation page soon.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorTokens.primerColorTextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
    }
}
