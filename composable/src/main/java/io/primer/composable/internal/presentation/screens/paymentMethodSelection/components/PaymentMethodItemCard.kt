package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.primer.composable.R
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSizeTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
internal fun PaymentMethodSelectionScope.PaymentMethodItemCard(
    modifier: Modifier = Modifier,
    onPaymentMethodSelected: () -> Unit,
) {
    PaymentMethodItem(
        modifier = modifier,
        borderColor = LocalPrimerColorTokens.current.primerColorBorderOutlinedDefault,
        onPaymentMethodSelected = onPaymentMethodSelected
    ) {
        Row {
            Icon(
                painter = painterResource(id = R.drawable.ic_primer_credit_card),
                contentDescription = null,
                tint = LocalPrimerColorTokens.current.primerColorTextPrimary,
                modifier = Modifier.size(LocalPrimerSizeTokens.current.medium)
            )
            Text(
                text = "Pay with card",
                style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                color = LocalPrimerColorTokens.current.primerColorTextPrimary,
                modifier = Modifier.padding(start = LocalPrimerSpacingTokens.current.small)
            )
        }

    }
}
