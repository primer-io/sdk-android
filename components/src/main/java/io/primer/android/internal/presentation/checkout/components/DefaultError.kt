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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.internal.presentation.components.PrimerButton
import io.primer.android.internal.presentation.preview.PreviewContainer

/**
 * Composition local for retry action on error screen.
 * Provided by SheetNavHost/FlowSheetOverlay.
 */
internal val LocalRetryAction = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Composition local for "try other methods" action on error screen.
 * Provided by SheetNavHost/FlowSheetOverlay. Null hides the button.
 */
internal val LocalOtherMethodsAction = staticCompositionLocalOf<(() -> Unit)?> { null }

/**
 * Default error screen shown after a failed payment.
 *
 * @param error The error from the failed payment
 * @param title Custom title text, or null to use default
 * @param message Custom message text, or null to use error description
 */
@Composable
internal fun DefaultError(
    error: PrimerError? = null,
    title: String? = null,
    message: String? = null,
) {
    val spacingTokens = LocalPrimerTheme.current.spacingTokens
    val sizeTokens = LocalPrimerTheme.current.sizeTokens

    Column(
        modifier = Modifier
            .padding(spacingTokens.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(sizeTokens.xxxlarge))

        ErrorIconSection()

        Spacer(modifier = Modifier.height(spacingTokens.small))

        ErrorTextSection(
            title = title,
            message = message,
            error = error,
        )

        Spacer(modifier = Modifier.height(sizeTokens.xxxlarge))

        ErrorActionButtons()
    }
}

@Composable
private fun ErrorIconSection() {
    Icon(
        painter = painterResource(id = R.drawable.ic_primer_checkout_error),
        contentDescription = stringResource(R.string.accessibility_checkout_error_icon),
        tint = Color.Unspecified,
    )
}

@Composable
private fun ErrorTextSection(
    title: String?,
    message: String?,
    error: PrimerError?,
) {
    val colorTokens = LocalPrimerTheme.current.colorTokens()
    val spacingTokens = LocalPrimerTheme.current.spacingTokens

    val displayTitle = title ?: stringResource(R.string.primer_checkout_error_title)
    val displayMessage = message
        ?: error?.description
        ?: stringResource(R.string.primer_checkout_error_subtitle)

    Text(
        text = displayTitle,
        color = colorTokens.primerColorTextPrimary,
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(spacingTokens.xsmall))

    Text(
        text = displayMessage,
        color = colorTokens.primerColorTextSecondary,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ErrorActionButtons() {
    val colorTokens = LocalPrimerTheme.current.colorTokens()
    val spacingTokens = LocalPrimerTheme.current.spacingTokens
    val titleLarge = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle()

    val onRetry = LocalRetryAction.current
    val onOtherMethods = LocalOtherMethodsAction.current

    PrimerButton(
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colorTokens.primerColorBrand,
    ) {
        Text(
            text = stringResource(R.string.primer_common_button_retry),
            style = titleLarge,
            color = colorTokens.primerColorBackground,
        )
    }

    // Only show "other methods" button if action is provided (not in inline mode)
    if (onOtherMethods != null) {
        Spacer(modifier = Modifier.height(spacingTokens.small))

        PrimerButton(
            onClick = onOtherMethods,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.Transparent,
            borderColor = colorTokens.primerColorBorderOutlinedDefault,
        ) {
            Text(
                text = stringResource(R.string.primer_checkout_error_button_other_methods),
                style = titleLarge,
                color = colorTokens.primerColorTextPrimary,
            )
        }
    }
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun DefaultErrorPreview() = PreviewContainer {
    DefaultError()
}
