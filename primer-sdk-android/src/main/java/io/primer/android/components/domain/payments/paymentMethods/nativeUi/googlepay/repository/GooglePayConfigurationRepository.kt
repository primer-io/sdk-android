package io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.repository

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.models.GooglePayConfiguration
import kotlinx.coroutines.flow.Flow

internal interface GooglePayConfigurationRepository {

    fun getConfiguration(): Flow<GooglePayConfiguration>
}
