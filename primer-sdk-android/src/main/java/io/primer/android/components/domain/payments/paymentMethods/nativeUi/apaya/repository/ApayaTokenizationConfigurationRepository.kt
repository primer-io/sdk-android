package io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.repository

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.models.ApayaTokenizationConfiguration
import kotlinx.coroutines.flow.Flow

internal interface ApayaTokenizationConfigurationRepository {

    fun getConfiguration(): Flow<ApayaTokenizationConfiguration>
}
