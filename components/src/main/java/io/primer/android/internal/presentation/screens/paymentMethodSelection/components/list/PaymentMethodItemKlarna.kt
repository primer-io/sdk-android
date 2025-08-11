package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

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
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.constants.PaymentMethodColors
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItemKlarna(
    modifier: Modifier = Modifier,
) {
    PaymentMethodItem(
        modifier = modifier,
        backgroundColor = LocalPrimerTheme.current.colorTokens().primerColorGray900,
        onPaymentMethodSelected = { onPaymentMethodSelected(PaymentMethodType.KLARNA.name) },
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.primer_components_payment_method_selection_klarna_pay_with),
                style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorGray000,
                modifier = Modifier.padding(end = LocalPrimerTheme.current.spacingTokens.small),
            )

            Box(
                modifier = modifier
                    .background(
                        color = PaymentMethodColors.klarnaPink,
                        shape = RoundedCornerShape(LocalPrimerTheme.current.radiusTokens.medium),
                    )
                    .padding(LocalPrimerTheme.current.spacingTokens.small),
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
