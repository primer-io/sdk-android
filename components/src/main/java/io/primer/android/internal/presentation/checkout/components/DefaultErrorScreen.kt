package io.primer.android.internal.presentation.checkout.components

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.primer.android.components.R
import io.primer.android.internal.presentation.components.PrimerButton
import io.primer.android.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.android.internal.presentation.theme.LocalPrimerSizeTokens
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.android.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.android.scope.PrimerCheckoutScope
import kotlinx.coroutines.launch

@Composable
internal fun PrimerCheckoutScope.DefaultErrorScreen(
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
) {
    val colorTokens = LocalPrimerColorTokens.current
    val spacingTokens = LocalPrimerSpacingTokens.current
    val sizeTokens = LocalPrimerSizeTokens.current
    val coroutineScope = rememberCoroutineScope()

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
            contentDescription = stringResource(R.string.primer_components_content_description_error),
            tint = Color.Unspecified,
        )

        Spacer(modifier = Modifier.height(spacingTokens.small))

        // Error title
        Text(
            text = title ?: stringResource(R.string.primer_components_checkout_failed_title),
            color = colorTokens.primerColorTextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(spacingTokens.xsmall))

        // Error message
        Text(
            text = message ?: stringResource(R.string.primer_components_checkout_failed_description),
            color = colorTokens.primerColorTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(sizeTokens.xxxlarge))

        PrimerButton(
            onClick = {
                coroutineScope.launch {
                    onRetry()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colorTokens.primerColorBrand,
        ) {
            Text(
                text = stringResource(R.string.primer_components_checkout_retry),
                style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                color = LocalPrimerColorTokens.current.primerColorBackground,
            )
        }

        Spacer(modifier = Modifier.height(spacingTokens.small))

        PrimerButton(
            onClick = {
                coroutineScope.launch {
                    onOtherPaymentMethods()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.Transparent,
            borderColor = LocalPrimerColorTokens.current.primerColorBorderOutlinedDefault,
        ) {
            Text(
                text = stringResource(R.string.primer_components_checkout_other_payment_methods),
                style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                color = LocalPrimerColorTokens.current.primerColorTextPrimary,
            )
        }
    }
}
