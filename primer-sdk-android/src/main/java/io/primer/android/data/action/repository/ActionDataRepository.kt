@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.data.action.repository

import io.primer.android.data.action.datasource.RemoteActionDataSource
import io.primer.android.data.action.models.ClientSessionActionsDataRequest
import io.primer.android.data.action.models.toActionData
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.ConfigurationCache
import io.primer.android.data.configuration.datasource.GlobalConfigurationCacheDataSource
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.utils.PrimerSessionConstants
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.domain.action.repository.ActionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit

internal class ActionDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val remoteActionDataSource: RemoteActionDataSource,
    private val globalConfigurationCache: GlobalConfigurationCacheDataSource,
    private val primerConfig: PrimerConfig,
    private val getCurrentTimeMillis: () -> Long = { System.currentTimeMillis() }
) : ActionRepository {

    override fun updateClientActions(params: BaseActionUpdateParams) =
        localConfigurationDataSource.get()
            .flatMapLatest {
                remoteActionDataSource.execute(
                    BaseRemoteRequest(
                        it,
                        ClientSessionActionsDataRequest(params.toActionData())
                    )
                )
            }.onEach { configurationResponse ->
                val ttlValue =
                    configurationResponse.headers[PrimerSessionConstants.PRIMER_SESSION_CACHE_TTL_HEADER]?.firstOrNull()
                        ?.toLongOrNull()
                        ?: PrimerSessionConstants.DEFAULT_SESSION_TTL_VALUE
                globalConfigurationCache.update(
                    ConfigurationCache(
                        validUntil = TimeUnit.SECONDS.toMillis(ttlValue) + getCurrentTimeMillis.invoke(),
                        clientToken = primerConfig.clientTokenBase64.orEmpty()
                    ) to configurationResponse.body
                )
            }.mapLatest { primerResponse -> primerResponse.body }.flatMapLatest { configuration ->
                localConfigurationDataSource.get().map { localConfiguration ->
                    localConfigurationDataSource.update(
                        localConfiguration
                            .copy(clientSession = configuration.clientSession)
                    )
                }.mapLatest { requireNotNull(configuration.clientSession).toClientSessionData() }
            }
}
