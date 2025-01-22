package io.primer.android.clientSessionActions.data.repository

import io.primer.android.clientSessionActions.data.datasource.RemoteActionDataSource
import io.primer.android.clientSessionActions.data.models.ClientSessionActionsDataRequest
import io.primer.android.clientSessionActions.data.models.toActionData
import io.primer.android.clientSessionActions.domain.models.BaseActionUpdateParams
import io.primer.android.clientSessionActions.domain.repository.ActionRepository
import io.primer.android.configuration.PrimerSessionConstants
import io.primer.android.configuration.data.datasource.ConfigurationCache
import io.primer.android.configuration.data.datasource.GlobalCacheConfigurationCacheDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.extensions.onError
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.errors.data.exception.SessionUpdateException
import java.util.concurrent.TimeUnit

internal class ActionDataRepository(
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val remoteActionDataSource: RemoteActionDataSource,
    private val globalCacheDataSource: GlobalCacheConfigurationCacheDataSource,
    private val clientTokenProvider: BaseDataProvider<String>,
    private val getCurrentTimeMillis: () -> Long = { System.currentTimeMillis() },
) : ActionRepository {
    override suspend fun updateClientActions(params: List<BaseActionUpdateParams>) =
        runSuspendCatching {
            configurationDataSource.get()
                .let {
                    remoteActionDataSource.execute(
                        BaseRemoteHostRequest(
                            host = it.pciUrl,
                            data = ClientSessionActionsDataRequest(params.flatMap { it.toActionData() }),
                        ),
                    )
                }.let { configuration ->
                    val ttlValue =
                        configuration.headers[PrimerSessionConstants.PRIMER_SESSION_CACHE_TTL_HEADER]?.firstOrNull()
                            ?.toLongOrNull()
                            ?: PrimerSessionConstants.DEFAULT_SESSION_TTL_VALUE

                    globalCacheDataSource.update(
                        ConfigurationCache(
                            validUntil = TimeUnit.SECONDS.toMillis(ttlValue) + getCurrentTimeMillis(),
                            clientToken = clientTokenProvider.provide(),
                        ) to configuration.body,
                    )

                    configurationDataSource.get().let { localConfiguration ->
                        configurationDataSource.update(
                            localConfiguration
                                .copy(clientSession = configuration.body.clientSession)
                                .copy(checkoutModules = configuration.body.checkoutModules),
                        )
                    }.let { configuration.body.clientSession.toClientSessionData() }
                }
        }.onError {
            when {
                it is HttpException && it.isClientError() ->
                    throw SessionUpdateException(
                        diagnosticsId = it.error.diagnosticsId,
                        description = it.error.description,
                    )

                else -> throw it
            }
        }
}
