package io.primer.android.domain.payments.methods

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.methods.models.PaymentMethodVaultTokenInternal
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

@ExperimentalCoroutinesApi
internal class VaultedPaymentMethodsInteractor(
    private val vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseInteractor<List<PaymentMethodVaultTokenInternal>, None>() {

    override fun execute(params: None) = vaultedPaymentMethodsRepository.getVaultedPaymentMethods()
        .mapLatest {
            it.filter {
                DISALLOWED_PAYMENT_METHOD_TYPES.contains(it.paymentInstrumentType).not()
            }
        }.flowOn(dispatcher)
        .catch {
            baseErrorEventResolver.resolve(it, ErrorMapperType.DEFAULT)
        }

    companion object {

        private val DISALLOWED_PAYMENT_METHOD_TYPES = listOf(PaymentMethodType.APAYA.name)
    }
}
