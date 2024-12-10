package io.primer.android.nolpay.implementation.common.presentation

import io.primer.android.core.domain.None
import io.primer.android.nolpay.implementation.common.domain.NolPaySdkInitInteractor

internal interface BaseNolPayDelegate {

    val sdkInitInteractor: NolPaySdkInitInteractor

    suspend fun start() = sdkInitInteractor(None)
}
