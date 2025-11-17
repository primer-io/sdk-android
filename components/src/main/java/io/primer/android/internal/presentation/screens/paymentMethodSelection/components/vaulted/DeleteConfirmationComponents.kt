package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

@Composable
internal fun DeleteConfirmationContent(
    paymentMethod: PrimerVaultedPaymentMethod,
    isDeleting: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val theme = LocalPrimerTheme.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = theme.spacingTokens.large),
        verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.large),
    ) {
        DeleteConfirmationPaymentMethod(paymentMethod)
        DeleteConfirmationMessage(paymentMethod)
        DeleteConfirmationActions(
            isDeleting = isDeleting,
            onCancel = onCancel,
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun DeleteConfirmationPaymentMethod(paymentMethod: PrimerVaultedPaymentMethod) {
    val theme = LocalPrimerTheme.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(theme.radiusTokens.medium),
        border = BorderStroke(
            width = 1.dp,
            color = theme.colorTokens().primerColorBorderOutlinedDefault,
        ),
    ) {
        val contentModifier = Modifier.padding(theme.spacingTokens.medium)
        VaultedPaymentMethodContent(
            paymentMethod = paymentMethod,
            isSelected = false,
            modifier = contentModifier,
            enableClickable = false,
        )
    }
}

@Composable
private fun DeleteConfirmationMessage(paymentMethod: PrimerVaultedPaymentMethod) {
    val theme = LocalPrimerTheme.current
    val paymentMethodName = when (paymentMethod.paymentMethodType) {
        PaymentMethodType.PAYMENT_CARD.name -> paymentMethod.paymentInstrumentData.network.orEmpty()
        else -> paymentMethod.paymentInstrumentType
    }.ifBlank { paymentMethod.paymentMethodType }

    Text(
        text = stringResource(
            R.string.primer_components_vaulted_delete_dialog_message,
            paymentMethodName,
        ),
        style = theme.typographyTokens.bodyMedium.toTextStyle(),
        color = theme.colorTokens().primerColorTextPrimary,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DeleteConfirmationActions(
    isDeleting: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val theme = LocalPrimerTheme.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            enabled = !isDeleting,
            shape = RoundedCornerShape(theme.radiusTokens.medium),
        ) {
            Text(
                text = stringResource(R.string.primer_components_vaulted_delete_dialog_cancel),
                style = theme.typographyTokens.bodyMedium.toTextStyle(),
                color = theme.colorTokens().primerColorTextPrimary,
            )
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            enabled = !isDeleting,
            shape = RoundedCornerShape(theme.radiusTokens.medium),
            colors = ButtonDefaults.buttonColors(
                containerColor = theme.colorTokens().primerColorBrand,
            ),
        ) {
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(R.string.primer_components_vaulted_delete_dialog_confirm),
                    style = theme.typographyTokens.bodyMedium.toTextStyle(),
                )
            }
        }
    }
}
