package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

/**
 * Default composable for displaying an individual vaulted payment method.
 * Shows payment method icon, details, and selection state.
 */
@Composable
internal fun VaultedPaymentMethodContent(
    paymentMethod: PrimerVaultedPaymentMethod,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enableClickable: Boolean = true,
) {
    when (paymentMethod.paymentMethodType) {
        PaymentMethodType.PAYMENT_CARD.name -> {
            VaultedCardPaymentMethodItem(
                paymentMethod = paymentMethod,
                isSelected = isSelected,
                modifier = modifier,
                onClick = onClick,
                enableClickable = enableClickable,
            )
        }
        else -> {
            VaultedOtherPaymentMethodItem(
                paymentMethod = paymentMethod,
                isSelected = isSelected,
                modifier = modifier,
            )
        }
    }
}

@Composable
internal fun VaultedCardPaymentMethodItem(
    paymentMethod: PrimerVaultedPaymentMethod,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enableClickable: Boolean = true,
) {
    val theme = LocalPrimerTheme.current
    val rowModifier = modifier.fillMaxWidth().let { base ->
        if (enableClickable) {
            base.clickable(onClick = onClick)
        } else {
            base
        }
    }

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VaultedPaymentMethodDetails(
            paymentMethod = paymentMethod,
            modifier = Modifier.weight(1f),
        )

        if (isSelected) {
            VaultedSelectionIndicator()
        }
    }
}

@Composable
private fun VaultedPaymentMethodDetails(
    paymentMethod: PrimerVaultedPaymentMethod,
    modifier: Modifier = Modifier,
) {
    val theme = LocalPrimerTheme.current
    val isCard = paymentMethod.paymentMethodType == PaymentMethodType.PAYMENT_CARD.name

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.xxsmall),
    ) {
        if (isCard) {
            VaultedCardDetails(paymentMethod)
        } else {
            VaultedNonCardDetails(paymentMethod)
        }
    }
}

@Composable
private fun VaultedCardDetails(paymentMethod: PrimerVaultedPaymentMethod) {
    VaultedCardHeaderRow(paymentMethod)
    VaultedCardInfoRow(paymentMethod)
}

@Composable
private fun VaultedCardHeaderRow(paymentMethod: PrimerVaultedPaymentMethod) {
    val theme = LocalPrimerTheme.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = resolveCardHolderNameFromPaymentMethod(paymentMethod),
            style = theme.typographyTokens.bodyLarge.toTextStyle(),
            color = theme.colorTokens().primerColorTextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f, fill = false),
        )

        Text(
            text = getPaymentMethodDetails(paymentMethod),
            style = theme.typographyTokens.bodyMedium.toTextStyle(),
            color = theme.colorTokens().primerColorTextPrimary,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun VaultedCardInfoRow(paymentMethod: PrimerVaultedPaymentMethod) {
    val theme = LocalPrimerTheme.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.xsmall),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false),
        ) {
            PaymentMethodIcon(
                network = paymentMethod.paymentInstrumentData.network,
                modifier = Modifier.size(theme.sizeTokens.large),
            )
            Text(
                text = paymentMethod.paymentInstrumentData.network.orEmpty(),
                style = theme.typographyTokens.bodySmall.toTextStyle(),
                color = theme.colorTokens().primerColorTextSecondary,
            )
        }

        Text(
            text = getPaymentMethodExpiry(paymentMethod),
            style = theme.typographyTokens.bodySmall.toTextStyle(),
            color = theme.colorTokens().primerColorTextSecondary,
        )
    }
}

@Composable
private fun VaultedNonCardDetails(paymentMethod: PrimerVaultedPaymentMethod) {
    val theme = LocalPrimerTheme.current

    Text(
        text = resolveCardHolderNameFromPaymentMethod(paymentMethod),
        style = theme.typographyTokens.bodyMedium.toTextStyle(),
        color = theme.colorTokens().primerColorTextPrimary,
        fontWeight = FontWeight.Medium,
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

@Composable
private fun VaultedSelectionIndicator() {
    val theme = LocalPrimerTheme.current

    Icon(
        painter = painterResource(id = R.drawable.ic_check_blue),
        contentDescription = stringResource(
            id = R.string.primer_components_vaulted_selected_content_description,
        ),
        modifier = Modifier.size(theme.sizeTokens.medium),
        tint = Color.Unspecified,
    )
}
