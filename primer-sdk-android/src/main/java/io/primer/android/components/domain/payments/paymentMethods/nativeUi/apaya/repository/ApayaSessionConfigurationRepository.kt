package io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.repository

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.models.ApayaSessionConfiguration
import kotlinx.coroutines.flow.Flow

internal interface ApayaSessionConfigurationRepository {

    fun getConfiguration(): Flow<ApayaSessionConfiguration>
}
