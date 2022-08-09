package io.primer.android.data.action.repository

import io.primer.android.data.action.datasource.RemoteActionDataSource
import io.primer.android.data.action.models.ClientSessionActionsDataRequest
import io.primer.android.data.action.models.toActionData
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.domain.action.repository.ActionRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

internal class ActionDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val remoteActionDataSource: RemoteActionDataSource,
) : ActionRepository {

    override fun updateClientActions(params: BaseActionUpdateParams) =
        localConfigurationDataSource.get()
            .flatMapLatest {
                remoteActionDataSource.execute(
                    BaseRemoteRequest(
                        it,
                        ClientSessionActionsDataRequest(listOf(params.toActionData()))
                    )
                )
            }.flatMapLatest { configuration ->
                localConfigurationDataSource.get().map { localConfiguration ->
                    localConfigurationDataSource.update(
                        localConfiguration
                            .copy(clientSession = configuration.clientSession)
                    )
                }.mapLatest { requireNotNull(configuration.clientSession).toClientSessionData() }
            }
}
