package io.primer.android.domain.payments.methods

import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.payments.methods.models.VaultDeleteParams
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
internal class VaultedPaymentMethodsDeleteInteractor(
    private val vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository,
    private val logger: Logger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseInteractor<String, VaultDeleteParams>() {

    override fun execute(params: VaultDeleteParams) =
        vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(
            params.id
        ).flowOn(dispatcher)
            .catch {
                logger.error(it.message.orEmpty())
            }.map { params.id }
}
