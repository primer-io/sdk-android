package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.surcharge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.domain.utils.UNKNOWN_SURCHARGE
import io.primer.android.internal.presentation.preview.PreviewContainer
import io.primer.android.internal.presentation.preview.mockPaymentMethods
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list.DefaultPaymentMethodItem

@Composable
internal fun SurchargeGroupCard(
    value: Int,
    paymentMethods: List<PrimerComposablePaymentMethod>,
    onPaymentMethodSelected: (String) -> Unit,
    formatAmount: (Int) -> String = { it.toString() },
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LocalPrimerTheme.current.spacingTokens.xsmall),
        shape = RoundedCornerShape(LocalPrimerTheme.current.sizeTokens.small),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalPrimerTheme.current.colorTokens().primerColorGray100)
                .padding(LocalPrimerTheme.current.spacingTokens.small),
            verticalArrangement = Arrangement.spacedBy(LocalPrimerTheme.current.spacingTokens.small),
        ) {
            SurchargeHeader(value = value, formatAmount = formatAmount)
            paymentMethods.forEach { paymentMethod ->
                DefaultPaymentMethodItem(
                    primerMethod = paymentMethod,
                    onPaymentMethodSelected = onPaymentMethodSelected,
                )
            }
        }
    }
}

@Composable
private fun SurchargeHeader(value: Int, formatAmount: (Int) -> String) {
    when (value) {
        0 -> {
            Text(
                text = stringResource(R.string.primer_payment_selection_surcharge_none),
                style = LocalPrimerTheme.current.typographyTokens.bodyMedium.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorGray900,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        UNKNOWN_SURCHARGE -> {
            Text(
                text = stringResource(R.string.primer_payment_selection_surcharge_may_apply),
                style = LocalPrimerTheme.current.typographyTokens.bodyMedium.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorGray900,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        else -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.primer_payment_selection_surcharge_label),
                    style = LocalPrimerTheme.current.typographyTokens.bodyMedium.toTextStyle(),
                    color = LocalPrimerTheme.current.colorTokens().primerColorTextSecondary,
                )
                Text(
                    text = "+ ${formatAmount(value)}",
                    style = LocalPrimerTheme.current.typographyTokens.bodyMedium.toTextStyle(),
                    color = LocalPrimerTheme.current.colorTokens().primerColorGray900,
                )
            }
        }
    }
}

@Preview(name = "No Surcharge", showBackground = true)
@Composable
private fun SurchargeGroupCardNoSurchargePreview() = PreviewContainer {
    SurchargeGroupCard(
        value = 0,
        paymentMethods = mockPaymentMethods.take(2),
        onPaymentMethodSelected = {},
        formatAmount = { "$${it / 100}.${(it % 100).toString().padStart(2, '0')}" },
    )
}

@Preview(name = "With Surcharge", showBackground = true)
@Composable
private fun SurchargeGroupCardWithSurchargePreview() = PreviewContainer {
    SurchargeGroupCard(
        value = 100,
        paymentMethods = mockPaymentMethods.take(2),
        onPaymentMethodSelected = {},
        formatAmount = { "$${it / 100}.${(it % 100).toString().padStart(2, '0')}" },
    )
}
