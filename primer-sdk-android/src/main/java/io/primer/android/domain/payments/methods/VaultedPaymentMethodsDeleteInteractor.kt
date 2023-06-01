package io.primer.android.domain.payments.methods

import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.payments.methods.models.VaultDeleteParams
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.extensions.onError
import io.primer.android.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class VaultedPaymentMethodsDeleteInteractor(
    private val vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository,
    private val logger: Logger,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<String, VaultDeleteParams>() {

    override suspend fun performAction(params: VaultDeleteParams) =
        vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(
            params.id
        ).map { params.id }.onError {
            logger.error(it.message.orEmpty())
            throw it
        }
}
