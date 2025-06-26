package io.primer.android.internal.presentation.screens.paymentMethodSelection.components

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
import androidx.compose.ui.res.stringResource
import io.primer.android.components.R
import io.primer.android.internal.presentation.constants.PaymentMethodColors
import io.primer.android.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.android.internal.presentation.theme.LocalPrimerRadiusTokens
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.android.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.primer_components_payment_method_selection_klarna_pay_with),
                style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                color = LocalPrimerColorTokens.current.primerColorGray000,
                modifier = Modifier.padding(end = LocalPrimerSpacingTokens.current.small),
            )

            Box(
                modifier = modifier
                    .background(
                        color = PaymentMethodColors.klarnaPink,
                        shape = RoundedCornerShape(LocalPrimerRadiusTokens.current.medium),
                    )
                    .padding(LocalPrimerSpacingTokens.current.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_primer_klarna_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            }
        }
    }
}
