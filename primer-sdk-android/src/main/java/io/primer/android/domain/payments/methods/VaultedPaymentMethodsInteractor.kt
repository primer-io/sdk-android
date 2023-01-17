package io.primer.android.domain.payments.methods

import io.primer.android.data.payments.methods.models.PaymentMethodVaultTokenInternal
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.methods.models.VaultInstrumentParams
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

@ExperimentalCoroutinesApi
internal class VaultedPaymentMethodsInteractor(
    private val vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseFlowInteractor<List<PaymentMethodVaultTokenInternal>, VaultInstrumentParams>() {

    override fun execute(params: VaultInstrumentParams) =
        getVaultedPaymentMethods(params.shouldFetch)
            .flowOn(dispatcher)
            .catch {
                baseErrorEventResolver.resolve(it, ErrorMapperType.DEFAULT)
            }

    private fun getVaultedPaymentMethods(shouldFetch: Boolean) = when (shouldFetch) {
        true -> vaultedPaymentMethodsRepository.getVaultedPaymentMethods()
        false -> flowOf(emptyList())
    }
}
