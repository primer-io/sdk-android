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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.preview.PreviewContainer
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

@Composable
internal fun PaymentMethodItemCard(
    onPaymentMethodSelected: (String) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    PaymentMethodItem(
        modifier = Modifier.testTag("primer_payment_method_card"),
        borderColor = LocalPrimerTheme.current.colorTokens().primerColorBorderOutlinedDefault,
        onPaymentMethodSelected = { onPaymentMethodSelected(PaymentMethodType.PAYMENT_CARD.name) },
        accessibilityLabel = stringResource(R.string.accessibility_payment_selection_pay_with_card),
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
                text = stringResource(R.string.primer_card_form_title),
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

@Preview(name = "Default", showBackground = true)
@Composable
private fun PaymentMethodItemCardPreview() = PreviewContainer {
    PaymentMethodItemCard(onPaymentMethodSelected = {})
}
