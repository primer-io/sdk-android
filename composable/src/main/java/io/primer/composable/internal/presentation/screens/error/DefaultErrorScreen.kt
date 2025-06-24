package io.primer.composable.internal.presentation.screens.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import io.primer.composable.R
import io.primer.composable.internal.presentation.components.PrimerButton
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSizeTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerTypographyTokens

@Composable
internal fun DefaultErrorScreen(
    modifier: Modifier = Modifier,
    title: String = "Payment failed",
    message: String = "There was a network issue.",
    onRetryClick: (() -> Unit)? = null,
    onOtherPaymentMethodClick: (() -> Unit)? = null,
) {
    val colorTokens = LocalPrimerColorTokens.current
    val spacingTokens = LocalPrimerSpacingTokens.current
    val sizeTokens = LocalPrimerSizeTokens.current

    Column(
        modifier = modifier
            .padding(spacingTokens.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Spacer(modifier = Modifier.height(sizeTokens.xxxlarge))
        // Error icon
        Icon(
            painter = painterResource(id = R.drawable.ic_primer_checkout_error),
            contentDescription = "Error",
            tint = Color.Unspecified,
        )
        
        Spacer(modifier = Modifier.height(spacingTokens.small))
        
        // Error title
        Text(
            text = title,
            color = colorTokens.primerColorTextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        
        Spacer(modifier = Modifier.height(spacingTokens.xsmall))
        
        // Error message
        Text(
            text = message,
            color = colorTokens.primerColorTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        
        Spacer(modifier = Modifier.height(sizeTokens.xxxlarge))
        
        // Retry button
        if (onRetryClick != null) {
            PrimerButton(
                onClick = onRetryClick,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colorTokens.primerColorBrand,
            ) {
                Text(
                    // TODO COMPOSABLE extract string resource
                    text = "Retry",
                    style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                    color = LocalPrimerColorTokens.current.primerColorBackground,
                )
            }
            
            Spacer(modifier = Modifier.height(spacingTokens.small))
        }
        
        // Choose other payment method link
        if (onOtherPaymentMethodClick != null) {
            PrimerButton(
                onClick = onOtherPaymentMethodClick,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.Transparent,
                borderColor = LocalPrimerColorTokens.current.primerColorBorderOutlinedDefault,
            ) {
                Text(
                    // TODO COMPOSABLE extract string resource
                    text = "Choose other payment methods",
                    style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                    color = LocalPrimerColorTokens.current.primerColorTextPrimary,
                )
            }
        }
    }
}
