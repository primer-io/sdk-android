package io.primer.android.vault.implementation.vaultedMethods.domain

import io.primer.android.components.domain.exception.InvalidVaultedPaymentMethodIdException
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.vault.implementation.vaultedMethods.data.model.toVaultedPaymentMethod
import io.primer.android.vault.implementation.vaultedMethods.domain.model.VaultPaymentMethodIdParams
import io.primer.android.vault.implementation.vaultedMethods.domain.repository.VaultedPaymentMethodsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class FindVaultedPaymentMethodInteractor(
    private val vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<PrimerVaultedPaymentMethod, VaultPaymentMethodIdParams>() {
    override suspend fun performAction(params: VaultPaymentMethodIdParams) =
        vaultedPaymentMethodsRepository.getVaultedPaymentMethods(true)
            .mapSuspendCatching { vaultedTokens ->
                vaultedTokens.firstOrNull { it.token == params.vaulterPaymentMethodId }
                    ?.toVaultedPaymentMethod()
                    ?: throw InvalidVaultedPaymentMethodIdException()
            }
}
