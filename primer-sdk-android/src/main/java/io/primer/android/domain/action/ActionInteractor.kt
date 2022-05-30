package io.primer.android.domain.action

import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.domain.action.repository.ActionRepository
import io.primer.android.domain.action.validator.ActionUpdateFilter
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
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
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseInteractor<Unit, BaseActionUpdateParams>() {

    val surcharges: Map<String, Int>
        get() = localConfigurationDataSource
            .getConfiguration()
            .clientSession
            ?.paymentMethod
            ?.surcharges
            ?: mapOf()

    val surchargeDataEmptyOrZero: Boolean
        get() = surcharges.all { item -> item.value == 0 } || surcharges.isEmpty()

    override fun execute(params: BaseActionUpdateParams) =
        actionUpdateFilter.filter(params).filterNot { it }.flatMapLatest {
            actionRepository.updateClientActions(
                params
            ).onStart {
                eventDispatcher.dispatchEvent(CheckoutEvent.ClientSessionUpdateStarted())
            }.doOnError {
                errorEventResolver.resolve(it, ErrorMapperType.ACTION_UPDATE)
            }.onEach {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.ClientSessionUpdateSuccess(it.clientSession)
                )
            }.map { }
        }.onEmpty { emit(Unit) }.flowOn(dispatcher)
}
