package io.primer.android.vault.implementation.vaultedMethods.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.onError
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.vault.implementation.vaultedMethods.domain.model.VaultDeleteParams
import io.primer.android.vault.implementation.vaultedMethods.domain.repository.VaultedPaymentMethodsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

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
