package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod

@Composable
internal fun VaultedOtherPaymentMethodItem(
    paymentMethod: PrimerVaultedPaymentMethod,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val theme = LocalPrimerTheme.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.xxsmall),
        ) {
            Text(
                text = resolveCardHolderNameFromPaymentMethod(paymentMethod),
                style = theme.typographyTokens.bodyMedium.toTextStyle(),
                color = theme.colorTokens().primerColorTextPrimary,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.xsmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PaymentMethodTypeIcon(
                    paymentMethodType = paymentMethod.paymentMethodType,
                    modifier = Modifier.size(theme.sizeTokens.large),
                )
                Text(
                    text = getPaymentMethodDetails(paymentMethod),
                    style = theme.typographyTokens.bodySmall.toTextStyle(),
                    color = theme.colorTokens().primerColorTextSecondary,
                )
            }
        }

        if (isSelected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check_blue),
                contentDescription = stringResource(
                    id = R.string.primer_components_vaulted_selected_content_description,
                ),
                modifier = Modifier.size(theme.sizeTokens.small),
                tint = androidx.compose.ui.graphics.Color.Unspecified,
            )
        }
    }
}
