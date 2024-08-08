package io.primer.android.threeds.data.repository

import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.threeds.data.datasource.Remote3DSAuthDataSource
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.auth.toBeginAuthRequest
import io.primer.android.threeds.data.models.postAuth.toContinueAuthDataRequest
import io.primer.android.threeds.domain.models.BaseThreeDsContinueAuthParams
import io.primer.android.threeds.domain.models.BaseThreeDsParams
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

internal class ThreeDsDataRepository(
    private val dataSource: Remote3DSAuthDataSource,
    private var configurationDataSource: LocalConfigurationDataSource
) : ThreeDsRepository {

    override fun begin3DSAuth(
        token: String,
        threeDsParams: BaseThreeDsParams
    ): Flow<BeginAuthResponse> {
        return configurationDataSource.get().flatMapLatest { configuration ->
            dataSource.get3dsAuthToken(
                configuration,
                token,
                threeDsParams.toBeginAuthRequest()
            ).mapLatest { responseBody -> responseBody.body }
        }
    }

    override fun continue3DSAuth(
        token: String,
        continueAuthParams: BaseThreeDsContinueAuthParams
    ) = configurationDataSource.get()
        .flatMapLatest { configuration ->
            dataSource.continue3dsAuth(
                configuration,
                token,
                continueAuthParams.toContinueAuthDataRequest()
            ).mapLatest { responseBody -> responseBody.body }
        }
}
