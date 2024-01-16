package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPaySdkInitInteractor
import io.primer.android.domain.base.None

internal open class BaseNolPayDelegate(
    private val sdkInitInteractor: NolPaySdkInitInteractor
) {
    suspend fun start() = sdkInitInteractor(None())
}
