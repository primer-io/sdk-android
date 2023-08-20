package io.primer.android.components.data.payments.paymentMethods.nolpay.repository

import io.primer.android.components.data.payments.paymentMethods.nolpay.datasource.RemoteNolPaySecretDataSource
import io.primer.android.components.data.payments.paymentMethods.nolpay.model.NolPaySecretDataRequest
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayAppSecretRepository
import io.primer.android.extensions.runSuspendCatching

internal class NolPayAppSecretDataRepository(
    private val nolPaySecretDataSource: RemoteNolPaySecretDataSource
) : NolPayAppSecretRepository {
    override suspend fun getAppSecret(sdkId: String) = runSuspendCatching {
        nolPaySecretDataSource.execute(NolPaySecretDataRequest(sdkId)).appSecret
    }
}
