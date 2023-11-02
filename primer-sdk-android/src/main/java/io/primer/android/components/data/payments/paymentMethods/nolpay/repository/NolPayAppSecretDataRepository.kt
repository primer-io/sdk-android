package io.primer.android.components.data.payments.paymentMethods.nolpay.repository

import android.os.Build
import io.primer.android.components.data.payments.paymentMethods.nolpay.datasource.RemoteNolPaySecretDataSource
import io.primer.android.components.data.payments.paymentMethods.nolpay.model.NolPaySecretDataRequest
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayAppSecretRepository
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.extensions.runSuspendCatching

internal class NolPayAppSecretDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val nolPaySecretDataSource: RemoteNolPaySecretDataSource
) : NolPayAppSecretRepository {
    override suspend fun getAppSecret(sdkId: String, appId: String) = runSuspendCatching {
        localConfigurationDataSource.getConfiguration().let { configuration ->
            nolPaySecretDataSource.execute(
                BaseRemoteRequest(
                    configuration,
                    NolPaySecretDataRequest(
                        sdkId,
                        appId,
                        Build.MANUFACTURER,
                        Build.MODEL
                    )
                )
            ).sdkSecret
        }
    }
}
