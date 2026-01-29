package io.primer.android.internal.presentation.utils

import androidx.compose.runtime.Composable
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod

data class VaultItemProperties(
    val paymentMethod: PrimerVaultedPaymentMethod,
    val isSelected: Boolean = false,
    val cvvLayout: @Composable () -> Unit = {},
    val onClick: () -> Unit = {},
)
