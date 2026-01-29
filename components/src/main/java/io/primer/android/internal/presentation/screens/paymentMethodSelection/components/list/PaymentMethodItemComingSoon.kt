package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
internal fun PaymentMethodItemComingSoon() {
    PaymentMethodItem(
        modifier = Modifier.testTag("primer_payment_method_coming_soon"),
        backgroundColor = LocalPrimerTheme.current.colorTokens().primerColorGray200,
        onPaymentMethodSelected = { },
        accessibilityLabel = stringResource(R.string.accessibility_payment_selection_coming_soon),
    ) {
        Text(
            text = stringResource(R.string.primer_misc_coming_soon),
            style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
            color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
        )
    }
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun PaymentMethodItemComingSoonPreview() = PreviewContainer {
    PaymentMethodItemComingSoon()
}
