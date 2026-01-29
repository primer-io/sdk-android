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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.constants.PaymentMethodColors
import io.primer.android.internal.presentation.preview.PreviewContainer
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

@Composable
internal fun PaymentMethodItemKlarna(
    onPaymentMethodSelected: (String) -> Unit,
) {
    PaymentMethodItem(
        modifier = Modifier.testTag("primer_payment_method_klarna"),
        backgroundColor = LocalPrimerTheme.current.colorTokens().primerColorGray900,
        onPaymentMethodSelected = { onPaymentMethodSelected(PaymentMethodType.KLARNA.name) },
        accessibilityLabel = stringResource(R.string.accessibility_payment_selection_pay_with_klarna),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.primer_klarna_pay_with),
                style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorGray000,
                modifier = Modifier.padding(end = LocalPrimerTheme.current.spacingTokens.small),
            )

            Box(
                modifier = Modifier
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

@Preview(name = "Default", showBackground = true)
@Composable
private fun PaymentMethodItemKlarnaPreview() = PreviewContainer {
    PaymentMethodItemKlarna(onPaymentMethodSelected = {})
}
