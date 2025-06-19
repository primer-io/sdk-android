package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import io.primer.composable.R
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerRadiusTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItemKlarna(
    modifier: Modifier = Modifier,
    onPaymentMethodSelected: () -> Unit,
) {
    PaymentMethodItem(
        modifier = modifier,
        backgroundColor = LocalPrimerColorTokens.current.primerColorGray900,
        onPaymentMethodSelected = onPaymentMethodSelected,
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                // TODO COMPOSABLE extract string resource
                text = "Pay with",
                style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                color = LocalPrimerColorTokens.current.primerColorGray000,
                modifier = Modifier.padding(end = LocalPrimerSpacingTokens.current.small),
            )

            // TODO COMPOSABLE check if this is correct
            Box(
                modifier = modifier
                    .background(
                        // TODO COMPOSABLE move this somewhere in constants
                        color = Color(0xFFFFA8CD),
                        shape = RoundedCornerShape(LocalPrimerRadiusTokens.current.medium),
                    )
                    .padding(LocalPrimerSpacingTokens.current.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_primer_klarna_logo),
                    // TODO COMPOSABLE content description
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            }
        }
    }
}
