package io.primer.android.domain.action

import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.domain.ClientSessionData
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.domain.action.models.MultipleActionUpdateParams
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.action.repository.ActionRepository
import io.primer.android.domain.action.validator.ActionUpdateFilter
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart

internal class ActionInteractor(
    private val actionRepository: ActionRepository,
    private val actionUpdateFilter: ActionUpdateFilter,
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<ClientSessionData, MultipleActionUpdateParams>() {

    val surcharges: Map<String, Int>
        get() = localConfigurationDataSource
            .getConfiguration()
            .clientSession
            .paymentMethod
            ?.surcharges
            ?: mapOf()

    val surchargeDataEmptyOrZero: Boolean
        get() = surcharges.all { item -> item.value == 0 } || surcharges.isEmpty()

    private var lastParams: MultipleActionUpdateParams? = null

    operator fun invoke(params: BaseActionUpdateParams): Flow<ClientSessionData> {
        return execute(MultipleActionUpdateParams(listOf(params)))
    }

    override fun execute(params: MultipleActionUpdateParams): Flow<ClientSessionData> {
        return flowOf(params.params)
            .filterNot { lastParams == params }
            .map { it.filterNot { actionUpdateFilter.filter(it) } }
            .filterNot { it.isEmpty() }
            .onEach { lastParams = params }
            .flatMapLatest { filteredList ->
                actionRepository.updateClientActions(filteredList)
                    .onStart {
                        eventDispatcher.dispatchEvent(CheckoutEvent.ClientSessionUpdateStarted())
                    }
                    .onEach {
                        eventDispatcher.dispatchEvent(CheckoutEvent.ClientSessionUpdateSuccess(it.clientSession))
                    }
                    .doOnError(dispatcher) {
                        errorEventResolver.resolve(it, ErrorMapperType.ACTION_UPDATE)
                    }
            }
            .onEmpty {
                emit(
                    ClientSessionData(
                        clientSession = PrimerClientSession(
                            customerId = null,
                            orderId = null,
                            currencyCode = null,
                            totalAmount = null,
                            lineItems = null,
                            orderDetails = null,
                            customer = null,
                            paymentMethod = null,
                            fees = null
                        )
                    )
                )
            }
            .flowOn(dispatcher)
    }
}
