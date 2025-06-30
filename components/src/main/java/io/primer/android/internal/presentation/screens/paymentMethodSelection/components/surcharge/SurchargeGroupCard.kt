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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.primer.android.components.R
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.domain.utils.UNKNOWN_SURCHARGE
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list.PaymentMethodSelector
import io.primer.android.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.android.internal.presentation.theme.LocalPrimerSizeTokens
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.android.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.android.internal.presentation.utils.CurrencyFormatter
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

@Composable
fun PrimerPaymentMethodSelectionScope.SurchargeGroupCard(
    value: Int,
    paymentMethods: List<PrimerComposablePaymentMethod>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LocalPrimerSpacingTokens.current.xsmall),
        shape = RoundedCornerShape(LocalPrimerSizeTokens.current.small),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalPrimerColorTokens.current.primerColorGray100)
                .padding(LocalPrimerSpacingTokens.current.small),
            verticalArrangement = Arrangement.spacedBy(LocalPrimerSpacingTokens.current.small)
        ) {
            SurchargeHeader(value = value)
            paymentMethods.forEach { paymentMethod ->
                PaymentMethodSelector(primerMethod = paymentMethod)
            }
        }
    }
}

@Composable
private fun PrimerPaymentMethodSelectionScope.SurchargeHeader(value: Int) {
    when (value) {
        0 -> {
            Text(
                text = stringResource(R.string.primer_components_surcharge_no_additional_fee),
                style = LocalPrimerTypographyTokens.current.bodyMedium.toTextStyle(),
                color = LocalPrimerColorTokens.current.primerColorGray900,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }

        UNKNOWN_SURCHARGE -> {
            Text(
                text = stringResource(R.string.primer_components_surcharge_additional_fees),
                style = LocalPrimerTypographyTokens.current.bodyMedium.toTextStyle(),
                color = LocalPrimerColorTokens.current.primerColorGray900,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }

        else -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.primer_components_surcharge_fee),
                    style = LocalPrimerTypographyTokens.current.bodyMedium.toTextStyle(),
                    color = LocalPrimerColorTokens.current.primerColorTextSecondary
                )
                Text(
                    text = formatSurcharge(value),
                    style = LocalPrimerTypographyTokens.current.bodyMedium.toTextStyle(),
                    color = LocalPrimerColorTokens.current.primerColorGray900
                )
            }
        }
    }
}

@Composable
private fun PrimerPaymentMethodSelectionScope.formatSurcharge(value: Int): String {
    val state by state.collectAsState()
    return (state as? PrimerPaymentMethodSelectionScope.State.Ready)?.orderInfo?.currencyCode?.let {
        "+ ${CurrencyFormatter.formatAmount(value, it)}"
    } ?: ""
}
