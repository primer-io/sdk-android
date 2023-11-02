package io.primer.android.domain.payments.methods

import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.payments.methods.models.VaultDeleteParams
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.extensions.onError
import io.primer.android.core.logging.internal.LogReporter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class VaultedPaymentMethodsDeleteInteractor(
    private val vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<String, VaultDeleteParams>() {

    override suspend fun performAction(params: VaultDeleteParams) =
        vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(
            params.id
        ).map { params.id }.onError { throwable ->
            logReporter.error(throwable.message.orEmpty(), throwable = throwable)
            throw throwable
        }
}
