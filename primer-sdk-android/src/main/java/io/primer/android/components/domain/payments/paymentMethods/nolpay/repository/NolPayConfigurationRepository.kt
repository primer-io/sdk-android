package io.primer.android.components.domain.payments.paymentMethods.nolpay.repository

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration

internal interface NolPayConfigurationRepository {

    suspend fun getConfiguration(): Result<NolPayConfiguration>
}
