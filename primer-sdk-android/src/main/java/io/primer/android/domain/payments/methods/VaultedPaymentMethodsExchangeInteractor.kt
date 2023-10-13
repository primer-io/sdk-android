package io.primer.android.domain.payments.methods

import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.methods.models.VaultTokenParams
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodExchangeRepository
import io.primer.android.domain.tokenization.helpers.PostTokenizationEventResolver
import io.primer.android.domain.tokenization.helpers.PreTokenizationEventsResolver
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
internal class VaultedPaymentMethodsExchangeInteractor(
    private val vaultedPaymentMethodExchangeRepository: VaultedPaymentMethodExchangeRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val preTokenizationEventsResolver: PreTokenizationEventsResolver,
    private val postTokenizationEventResolver: PostTokenizationEventResolver,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<PaymentMethodTokenInternal, VaultTokenParams>() {

    override fun execute(params: VaultTokenParams): Flow<PaymentMethodTokenInternal> {
        return flow {
            emit(
                preTokenizationEventsResolver.resolve(
                    params.paymentMethodType
                )
            )
        }.flatMapLatest {
            vaultedPaymentMethodExchangeRepository.exchangeVaultedPaymentToken(
                params.vaultedPaymentMethodId,
                params.additionalData
            ).flowOn(dispatcher)
                .onEach {
                    paymentMethodRepository.setPaymentMethod(it)
                    postTokenizationEventResolver.resolve(it)
                }
        }.catch {
            baseErrorEventResolver.resolve(it, ErrorMapperType.DEFAULT)
        }
    }
}
