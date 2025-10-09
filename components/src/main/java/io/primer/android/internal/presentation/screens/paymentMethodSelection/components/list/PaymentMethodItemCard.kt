package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItemCard(
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current

    PaymentMethodItem(
        borderColor = LocalPrimerTheme.current.colorTokens().primerColorBorderOutlinedDefault,
        onPaymentMethodSelected = { onPaymentMethodSelected(PaymentMethodType.PAYMENT_CARD.name) },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (layoutDirection == LayoutDirection.Rtl) {
                Arrangement.End
            } else {
                Arrangement.Start
            },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_primer_credit_card),
                contentDescription = null,
                tint = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
                modifier = Modifier.size(LocalPrimerTheme.current.sizeTokens.medium),
            )
            Text(
                text = stringResource(R.string.primer_components_select_payment_method_card),
                style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
                modifier = Modifier.padding(
                    start = if (layoutDirection == LayoutDirection.Ltr) {
                        LocalPrimerTheme.current.spacingTokens.small
                    } else {
                        0.dp
                    },
                    end = if (layoutDirection == LayoutDirection.Rtl) {
                        LocalPrimerTheme.current.spacingTokens.small
                    } else {
                        0.dp
                    },
                ),
            )
        }
    }
}
