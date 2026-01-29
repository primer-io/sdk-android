package io.primer.android.internal.presentation.checkout.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.internal.presentation.preview.PreviewContainer

/**
 * Default success screen shown after a successful payment.
 *
 * Displays a success icon and message. The screen is shown for 3 seconds
 * before the result is delivered and sheet is dismissed (handled by ViewModel).
 *
 * @param checkoutData Checkout data for displaying order information
 * @param title Custom title text, or null to use default
 * @param message Custom message text, or null to use default
 */
@Composable
internal fun DefaultSuccess(
    checkoutData: PrimerCheckoutData,
    title: String? = null,
    message: String? = null,
) {
    val spacing = LocalPrimerTheme.current.spacingTokens
    val sizes = LocalPrimerTheme.current.sizeTokens
    val colorTokens = LocalPrimerTheme.current.colorTokens()

    val displayTitle = title ?: stringResource(R.string.primer_checkout_success_title)
    val displayMessage = message ?: stringResource(R.string.primer_checkout_success_subtitle)

    Column(
        modifier = Modifier
            .padding(sizes.xxxlarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_primer_success),
            contentDescription = stringResource(R.string.accessibility_checkout_success_icon),
            tint = Color.Unspecified,
        )

        Spacer(modifier = Modifier.height(spacing.small))

        Text(
            text = displayTitle,
            style = MaterialTheme.typography.headlineSmall,
            color = colorTokens.primerColorTextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(spacing.xsmall))

        Text(
            text = displayMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = colorTokens.primerColorTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun DefaultSuccessPreview() = PreviewContainer {
    DefaultSuccess(
        checkoutData = PrimerCheckoutData(
            payment = Payment.undefined,
        ),
    )
}
