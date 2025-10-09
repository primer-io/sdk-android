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
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.domain.utils.UNKNOWN_SURCHARGE
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

@Composable
fun PrimerPaymentMethodSelectionScope.SurchargeGroupCard(
    value: Int,
    paymentMethods: List<PrimerComposablePaymentMethod>,
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
            SurchargeHeader(value = value)
            paymentMethods.forEach { paymentMethod ->
                components.paymentMethodItem(this@SurchargeGroupCard, paymentMethod)
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
                style = LocalPrimerTheme.current.typographyTokens.bodyMedium.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorGray900,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        UNKNOWN_SURCHARGE -> {
            Text(
                text = stringResource(R.string.primer_components_surcharge_additional_fees),
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
                    text = stringResource(R.string.primer_components_surcharge_fee),
                    style = LocalPrimerTheme.current.typographyTokens.bodyMedium.toTextStyle(),
                    color = LocalPrimerTheme.current.colorTokens().primerColorTextSecondary,
                )
                Text(
                    text = formatSurchargeAmount(value),
                    style = LocalPrimerTheme.current.typographyTokens.bodyMedium.toTextStyle(),
                    color = LocalPrimerTheme.current.colorTokens().primerColorGray900,
                )
            }
        }
    }
}
