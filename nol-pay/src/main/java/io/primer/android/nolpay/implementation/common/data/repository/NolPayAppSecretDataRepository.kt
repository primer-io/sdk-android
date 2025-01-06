package io.primer.android.nolpay.implementation.common.data.repository

import android.os.Build
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.nolpay.implementation.common.data.datasource.RemoteNolPaySecretDataSource
import io.primer.android.nolpay.implementation.common.data.model.NolPaySecretDataRequest
import io.primer.android.nolpay.implementation.common.domain.repository.NolPayAppSecretRepository

internal class NolPayAppSecretDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
    private val nolPaySecretDataSource: RemoteNolPaySecretDataSource,
) : NolPayAppSecretRepository {
    override suspend fun getAppSecret(
        sdkId: String,
        appId: String,
    ) = runSuspendCatching {
        configurationDataSource.get().let { configuration ->
            nolPaySecretDataSource.execute(
                BaseRemoteHostRequest(
                    host = configuration.coreUrl,
                    data =
                        NolPaySecretDataRequest(
                            sdkId = sdkId,
                            appId = appId,
                            deviceVendor = Build.MANUFACTURER,
                            deviceModel = Build.MODEL,
                        ),
                ),
            ).sdkSecret
        }
    }
}
