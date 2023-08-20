package io.primer.android.components.domain.payments.paymentMethods.nolpay.repository

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import kotlinx.coroutines.flow.Flow

internal interface NolPayConfigurationRepository {

    fun getConfiguration(): Flow<NolPayConfiguration>
}
