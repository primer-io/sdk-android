package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
internal fun PaymentMethodSelectionScope.PaymentMethodItemKlarna(
    modifier: Modifier = Modifier,
    onPaymentMethodSelected: () -> Unit,
) {
    PaymentMethodItem(
        borderColor = LocalPrimerColorTokens.current.primerColorBorderOutlinedDefault,
        backgroundColor = LocalPrimerColorTokens.current.primerColorBackground,
        onPaymentMethodSelected = onPaymentMethodSelected
    ) {
        Row(
            modifier = modifier
                .padding(
                    horizontal = LocalPrimerSpacingTokens.current.small,
                    vertical = LocalPrimerSpacingTokens.current.medium
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {

        }
    }
}
