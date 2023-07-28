package io.primer.android.components.domain.payments.vault

import io.primer.android.data.payments.methods.models.toVaultedPaymentMethod
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class HeadlessVaultedPaymentMethodsInteractor(
    private val vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<List<PrimerVaultedPaymentMethod>, None>() {

    override suspend fun performAction(params: None) =
        vaultedPaymentMethodsRepository.getVaultedPaymentMethods(false)
            .map { vaultedTokens ->
                vaultedTokens.map { vaultedToken -> vaultedToken.toVaultedPaymentMethod() }
            }
}
