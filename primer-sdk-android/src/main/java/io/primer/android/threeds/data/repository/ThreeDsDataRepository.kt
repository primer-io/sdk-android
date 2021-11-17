package io.primer.android.threeds.data.repository

import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.threeds.data.datasource.Remote3DSAuthDataSource
import io.primer.android.threeds.data.models.BeginAuthRequest
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import kotlinx.coroutines.flow.flatMapLatest

internal class ThreeDsDataRepository(
    private val dataSource: Remote3DSAuthDataSource,
    private var configurationDataSource: LocalConfigurationDataSource,
) : ThreeDsRepository {

    override fun begin3DSAuth(
        token: String,
        request: BeginAuthRequest,
    ) = configurationDataSource.get().flatMapLatest { configuration ->
        dataSource.get3dsAuthToken(
            configuration,
            token,
            request
        )
    }

    override fun continue3DSAuth(token: String) = configurationDataSource.get()
        .flatMapLatest { configuration ->
            dataSource.continue3dsAuth(
                configuration,
                token,
            )
        }
}
