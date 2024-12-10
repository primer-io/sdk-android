package io.primer.android.vault.implementation.vaultedMethods.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.domain.None
import io.primer.android.vault.implementation.vaultedMethods.data.model.toVaultedPaymentMethod
import io.primer.android.vault.implementation.vaultedMethods.domain.repository.VaultedPaymentMethodsRepository
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class FetchVaultedPaymentMethodsInteractor(
    private val vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<List<PrimerVaultedPaymentMethod>, None>() {

    override suspend fun performAction(params: None) =
        vaultedPaymentMethodsRepository.getVaultedPaymentMethods(false)
            .map { vaultedTokens ->
                vaultedTokens.map { vaultedToken -> vaultedToken.toVaultedPaymentMethod() }
            }
}
