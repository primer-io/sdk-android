package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import io.primer.android.scope.PrimerVaultedScope

internal data class VaultedMethodCardState(
    val isSelected: Boolean,
    val editMode: PrimerVaultedScope.State.EditMode,
    val isDeleting: Boolean,
)
