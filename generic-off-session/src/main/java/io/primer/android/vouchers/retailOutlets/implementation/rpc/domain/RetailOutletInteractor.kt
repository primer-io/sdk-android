package io.primer.android.vouchers.retailOutlets.implementation.rpc.domain

import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutlet
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutletParams
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.repository.RetailOutletRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class RetailOutletInteractor(
    private val configurationRepository: ConfigurationRepository,
    private val retailOutletRepository: RetailOutletRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<List<RetailOutlet>, RetailOutletParams>() {
    override suspend fun performAction(params: RetailOutletParams) =
        runSuspendCatching {
            configurationRepository.getConfiguration().paymentMethods.first { it.type == params.paymentMethodType }
        }.flatMap { configuration ->
            retailOutletRepository.getRetailOutlets(
                requireNotNullCheck(
                    configuration.id,
                    AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID,
                ),
            )
        }
            .map { it.filterNot { it.disabled } }
            .map { it.sortedBy { it.name.lowercase() } }
}
