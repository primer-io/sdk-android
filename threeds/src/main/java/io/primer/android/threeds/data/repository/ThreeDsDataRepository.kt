package io.primer.android.threeds.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.threeds.data.datasource.Remote3DSAuthDataSource
import io.primer.android.threeds.data.models.auth.toBeginAuthRequest
import io.primer.android.threeds.data.models.postAuth.toContinueAuthDataRequest
import io.primer.android.threeds.domain.models.BaseThreeDsContinueAuthParams
import io.primer.android.threeds.domain.models.BaseThreeDsParams
import io.primer.android.threeds.domain.repository.ThreeDsRepository

internal class ThreeDsDataRepository(
    private val dataSource: Remote3DSAuthDataSource,
    private val configurationDataSource: CacheConfigurationDataSource,
) : ThreeDsRepository {
    override suspend fun begin3DSAuth(
        token: String,
        threeDsParams: BaseThreeDsParams,
    ) = runSuspendCatching {
        configurationDataSource.get().let { configuration ->
            dataSource.get3dsAuthToken(
                configuration = configuration,
                paymentMethodToken = token,
                beginAuthRequest = threeDsParams.toBeginAuthRequest(),
            )
        }.body
    }

    override suspend fun continue3DSAuth(
        token: String,
        continueAuthParams: BaseThreeDsContinueAuthParams,
    ) = runSuspendCatching {
        configurationDataSource.get().let { configuration ->
            dataSource.continue3dsAuth(
                configuration,
                token,
                continueAuthParams.toContinueAuthDataRequest(),
            ).body
        }
    }
}
