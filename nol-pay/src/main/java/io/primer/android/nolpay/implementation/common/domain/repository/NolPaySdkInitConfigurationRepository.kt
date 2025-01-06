package io.primer.android.nolpay.implementation.common.domain.repository

import io.primer.android.nolpay.implementation.common.domain.model.NolPayConfiguration

internal fun interface NolPaySdkInitConfigurationRepository {
    suspend fun getConfiguration(): Result<NolPayConfiguration>
}
