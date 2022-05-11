package io.primer.android.domain.tokenization

import io.primer.android.PaymentMethodIntent
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.tokenization.helpers.PostTokenizationEventResolver
import io.primer.android.domain.tokenization.helpers.PreTokenizationEventsResolver
import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.domain.tokenization.repository.TokenizationRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.doOnError
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator.Companion.THREE_DS_CLASS_NOT_LOADED_ERROR
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class TokenizationInteractor(
    private val tokenizationRepository: TokenizationRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val threeDsSdkClassValidator: ThreeDsSdkClassValidator,
    private val preTokenizationEventsResolver: PreTokenizationEventsResolver,
    private val postTokenizationEventResolver: PostTokenizationEventResolver,
    private val errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseInteractor<String, TokenizationParams>() {

    override fun execute(params: TokenizationParams): Flow<String> {
        return flow {
            emit(preTokenizationEventsResolver.resolve(params.paymentMethodDescriptor.config.type))
        }.flatMapLatest {
            tokenizationRepository.tokenize(params)
                .onEach {
                    val token = PaymentMethodTokenAdapter.internalToExternal(it)
                    paymentMethodRepository.setPaymentMethod(it)
                    val perform3ds =
                        token.paymentInstrumentType == PaymentMethodType.PAYMENT_CARD.name &&
                            params.is3DSOnVaultingEnabled &&
                            params.paymentMethodIntent == PaymentMethodIntent.VAULT &&
                            params.paymentMethodDescriptor.config.options?.threeDSecureEnabled ==
                            true
                    when {
                        perform3ds -> {
                            if (threeDsSdkClassValidator.is3dsSdkIncluded()) {
                                eventDispatcher.dispatchEvent(CheckoutEvent.Start3DS())
                            } else postTokenizationEventResolver.resolve(
                                it.setClientThreeDsError(THREE_DS_CLASS_NOT_LOADED_ERROR)
                            )
                        }
                        else -> postTokenizationEventResolver.resolve(it)
                    }
                }
                .map { it.token }
        }
            .doOnError {
                errorEventResolver.resolve(it, ErrorMapperType.DEFAULT)
            }
            .flowOn(dispatcher)
    }
}
