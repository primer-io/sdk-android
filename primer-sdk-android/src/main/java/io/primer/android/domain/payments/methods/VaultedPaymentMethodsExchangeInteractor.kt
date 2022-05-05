package io.primer.android.domain.payments.methods

import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.methods.models.VaultTokenParams
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.domain.tokenization.helpers.PostTokenizationEventResolver
import io.primer.android.domain.tokenization.helpers.PreTokenizationEventsResolver
import io.primer.android.model.dto.PaymentMethodType
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
    private val vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val preTokenizationEventsResolver: PreTokenizationEventsResolver,
    private val postTokenizationEventResolver: PostTokenizationEventResolver,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseInteractor<PaymentMethodTokenInternal, VaultTokenParams>() {

    override fun execute(params: VaultTokenParams): Flow<PaymentMethodTokenInternal> {
        return flow {
            emit(
                preTokenizationEventsResolver.resolve(
                    PaymentMethodType.safeValueOf(
                        params.token.paymentInstrumentData?.paymentMethodType
                    )
                )
            )
        }.flatMapLatest {
            vaultedPaymentMethodsRepository.exchangeVaultedPaymentToken(
                params.token.token
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
