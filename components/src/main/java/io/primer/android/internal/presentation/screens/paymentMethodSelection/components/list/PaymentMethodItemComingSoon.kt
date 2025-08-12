package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItemComingSoon(
    onPaymentMethodSelected: () -> Unit,
) {
    PaymentMethodItem(
        backgroundColor = LocalPrimerTheme.current.colorTokens().primerColorGray200,
        onPaymentMethodSelected = onPaymentMethodSelected,
    ) {
        Text(
            text = stringResource(R.string.primer_components_select_payment_method_coming_soon),
            style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
            color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
        )
    }
}
