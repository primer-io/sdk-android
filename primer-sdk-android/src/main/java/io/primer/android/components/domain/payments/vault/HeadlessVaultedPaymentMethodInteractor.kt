package io.primer.android.components.domain.payments.vault

import io.primer.android.components.domain.exception.InvalidVaultedPaymentMethodIdException
import io.primer.android.components.domain.payments.models.VaultPaymentMethodIdParams
import io.primer.android.data.payments.methods.models.toVaultedPaymentMethod
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethodData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class HeadlessVaultedPaymentMethodInteractor(
    private val vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PrimerVaultedPaymentMethodData, VaultPaymentMethodIdParams>() {

    override suspend fun performAction(params: VaultPaymentMethodIdParams) =
        vaultedPaymentMethodsRepository.getVaultedPaymentMethods(true)
            .map { vaultedTokens ->
                vaultedTokens.firstOrNull { it.token == params.vaulterPaymentMethodId }
                    ?.toVaultedPaymentMethod()
                    ?: throw InvalidVaultedPaymentMethodIdException()
            }
}
