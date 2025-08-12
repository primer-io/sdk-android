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
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.components.PrimerButton
import io.primer.android.scope.PrimerCheckoutScope
import kotlinx.coroutines.launch

@Composable
internal fun PrimerCheckoutScope.DefaultErrorScreen(
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
) {
    val spacingTokens = LocalPrimerTheme.current.spacingTokens
    val sizeTokens = LocalPrimerTheme.current.sizeTokens

    Column(
        modifier = modifier
            .padding(spacingTokens.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(sizeTokens.xxxlarge))

        ErrorIconSection()

        Spacer(modifier = Modifier.height(spacingTokens.small))

        ErrorTextSection(title = title, message = message)

        Spacer(modifier = Modifier.height(sizeTokens.xxxlarge))

        ErrorActionButtons()
    }
}

@Composable
private fun ErrorIconSection() {
    Icon(
        painter = painterResource(id = R.drawable.ic_primer_checkout_error),
        contentDescription = stringResource(R.string.primer_components_content_description_error),
        tint = Color.Unspecified,
    )
}

@Composable
private fun ErrorTextSection(
    title: String?,
    message: String?,
) {
    val colorTokens = LocalPrimerTheme.current.colorTokens()
    val spacingTokens = LocalPrimerTheme.current.spacingTokens

    Text(
        text = title ?: stringResource(R.string.primer_components_checkout_failed_title),
        color = colorTokens.primerColorTextPrimary,
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(spacingTokens.xsmall))

    Text(
        text = message ?: stringResource(R.string.primer_components_checkout_failed_description),
        color = colorTokens.primerColorTextSecondary,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PrimerCheckoutScope.ErrorActionButtons() {
    val colorTokens = LocalPrimerTheme.current.colorTokens()
    val spacingTokens = LocalPrimerTheme.current.spacingTokens
    val titleLarge = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle()
    val coroutineScope = rememberCoroutineScope()

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
            style = titleLarge,
            color = colorTokens.primerColorBackground,
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
        borderColor = colorTokens.primerColorBorderOutlinedDefault,
    ) {
        Text(
            text = stringResource(R.string.primer_components_checkout_other_payment_methods),
            style = titleLarge,
            color = colorTokens.primerColorTextPrimary,
        )
    }
}
