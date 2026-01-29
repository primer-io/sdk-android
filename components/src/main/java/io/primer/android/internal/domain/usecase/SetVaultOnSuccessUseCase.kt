package io.primer.android.internal.domain.usecase

import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateVaultOnSuccessParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams

internal class SetVaultOnSuccessUseCase(
    private val actionInteractor: ActionInteractor,
) {

    suspend operator fun invoke(vaultOnSuccess: Boolean) = actionInteractor(
        MultipleActionUpdateParams(
            listOf(
                ActionUpdateVaultOnSuccessParams(vaultOnSuccess),
            ),
        ),
    )
}
