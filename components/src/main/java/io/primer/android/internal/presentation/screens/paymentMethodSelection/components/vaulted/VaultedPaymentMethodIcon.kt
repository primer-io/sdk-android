package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.primer.android.components.R
import io.primer.android.components.assets.ui.getCardImageAsset
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

@Composable
internal fun PaymentMethodIcon(
    network: String?,
    modifier: Modifier = Modifier,
) {
    val cardNetworkType = CardNetwork.Type.valueOrNull(network)
    val contentDescription = network?.let {
        stringResource(id = R.string.primer_components_content_description_card_network, it)
    } ?: stringResource(id = R.string.primer_components_vaulted_generic_card_content_description)

    if (cardNetworkType != null) {
        val iconResId = cardNetworkType.getCardImageAsset(
            io.primer.android.displayMetadata.domain.model.ImageColor.COLORED,
        )
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = Color.Unspecified,
        )
    } else {
        // Fallback to generic card icon
        Icon(
            painter = painterResource(id = R.drawable.ic_primer_credit_card),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = Color.Unspecified,
        )
    }
}

/**
 * Displays the icon for a payment method type (PayPal, ACH, etc.)
 */
@Composable
internal fun PaymentMethodTypeIcon(
    paymentMethodType: String,
    modifier: Modifier = Modifier,
) {
    val iconResId = when (paymentMethodType) {
        PaymentMethodType.PAYPAL.name -> R.drawable.ic_primer_paypal_icon
        PaymentMethodType.STRIPE_ACH.name -> R.drawable.ic_bank_16
        PaymentMethodType.PAYMENT_CARD.name -> R.drawable.ic_primer_credit_card
        else -> R.drawable.ic_primer_credit_card // Fallback
    }

    val contentDescription = when (paymentMethodType) {
        PaymentMethodType.PAYPAL.name -> "PayPal"
        PaymentMethodType.STRIPE_ACH.name -> "Bank Account"
        else -> paymentMethodType
    }

    Icon(
        painter = painterResource(id = iconResId),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = Color.Unspecified,
    )
}
