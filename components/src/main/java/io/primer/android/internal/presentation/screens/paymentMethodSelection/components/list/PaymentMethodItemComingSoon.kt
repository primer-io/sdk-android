package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.primer.android.components.R
import io.primer.android.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.android.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItemComingSoon(
    modifier: Modifier = Modifier,
    onPaymentMethodSelected: () -> Unit,
) {
    PaymentMethodItem(
        backgroundColor = LocalPrimerColorTokens.current.primerColorGray200,
        onPaymentMethodSelected = onPaymentMethodSelected,
    ) {
        Text(
            text = stringResource(R.string.primer_components_select_payment_method_coming_soon),
            style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
            color = LocalPrimerColorTokens.current.primerColorTextPrimary,
        )
    }
}
