package io.primer.android.domain.tokenization

import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.tokenization.helpers.PostTokenizationEventResolver
import io.primer.android.domain.tokenization.helpers.PreTokenizationEventsResolver
import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.repository.TokenizationRepository
import io.primer.android.extensions.doOnError
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

internal class TokenizationInteractor(
    private val tokenizationRepository: TokenizationRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val preTokenizationEventsResolver: PreTokenizationEventsResolver,
    private val postTokenizationEventResolver: PostTokenizationEventResolver,
    private val logReporter: LogReporter,
    private val errorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<String, TokenizationParams>() {

    override fun execute(params: TokenizationParams): Flow<String> {
        return flow {
            emit(
                preTokenizationEventsResolver.resolve(
                    params.paymentMethodDescriptor.config.type,
                    params.paymentMethodIntent
                )
            )
        }.flatMapLatest {
            val paymentMethodType = params.paymentMethodDescriptor.config.type
            tokenizationRepository.tokenize(params)
                .onStart {
                    logReporter.info(
                        "Started tokenization for $paymentMethodType payment method."
                    )
                }
                .onEach {
                    logReporter.info(
                        "Tokenization successful for $paymentMethodType payment method."
                    )
                    paymentMethodRepository.setPaymentMethod(it)
                    postTokenizationEventResolver.resolve(it, params.paymentMethodIntent)
                }
                .map { it.token }
        }
            .doOnError {
                errorEventResolver.resolve(it, ErrorMapperType.DEFAULT)
            }
            .flowOn(dispatcher)
    }

    fun executeV2(params: TokenizationParamsV2): Flow<String> {
        return flow {
            emit(
                preTokenizationEventsResolver.resolve(
                    params.paymentInstrumentParams.paymentMethodType,
                    params.paymentMethodIntent
                )
            )
        }.flatMapLatest {
            val paymentMethodType = params.paymentInstrumentParams.paymentMethodType
            tokenizationRepository.tokenize(params)
                .onStart {
                    logReporter.info(
                        "Started tokenization for $paymentMethodType payment method."
                    )
                }
                .onEach {
                    logReporter.info(
                        "Tokenization successful for $paymentMethodType payment method."
                    )
                    paymentMethodRepository.setPaymentMethod(it)
                    postTokenizationEventResolver.resolve(it, params.paymentMethodIntent)
                }
                .map { it.token }
        }
            .doOnError {
                errorEventResolver.resolve(it, ErrorMapperType.DEFAULT)
            }
            .flowOn(dispatcher)
    }
}
