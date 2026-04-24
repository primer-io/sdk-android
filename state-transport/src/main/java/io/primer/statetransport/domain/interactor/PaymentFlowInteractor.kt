package io.primer.statetransport.domain.interactor

import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.BaseFlowInteractor
import io.primer.android.core.domain.Params
import io.primer.statetransport.domain.model.ClientInstructions
import io.primer.statetransport.domain.repository.StateTransportRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

class PaymentFlowInteractor(
    private val repository: StateTransportRepository,
    private val configurationRepository: ConfigurationRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseFlowInteractor<ClientInstructions, PaymentFlowInteractor.PaymentFlowParams>() {

    override fun execute(params: PaymentFlowParams): Flow<ClientInstructions> = flow {
        val clientSessionId = requireNotNull(
            configurationRepository.getConfiguration()
                .clientSession.clientSessionDataResponse.clientSessionId,
        )
        val payResult = repository.start(
            clientSessionId = clientSessionId,
            paymentMethodType = params.paymentMethodType,
            returnUri = params.returnUri,
        ).getOrThrow()

        if (payResult !is ClientInstructions.Wait) {
            emit(payResult)
            if (payResult is ClientInstructions.End) return@flow
        }

        var pollDelay = when (payResult) {
            is ClientInstructions.Wait -> payResult.pollDelayMilliseconds
            is ClientInstructions.Execute -> payResult.pollDelayMilliseconds
            else -> return@flow
        }

        while (currentCoroutineContext().isActive) {
            delay(pollDelay)
            when (val instructions = repository.fetchInstructions(clientSessionId).getOrThrow()) {
                is ClientInstructions.Execute -> {
                    pollDelay = instructions.pollDelayMilliseconds
                    emit(instructions)
                }

                is ClientInstructions.End -> {
                    emit(instructions)
                    return@flow
                }

                is ClientInstructions.Wait -> {
                    pollDelay = instructions.pollDelayMilliseconds
                }
            }
        }
    }.flowOn(dispatcher)

    data class PaymentFlowParams(
        val paymentMethodType: String,
        val returnUri: String,
    ) : Params
}
