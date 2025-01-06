package io.primer.android.payments.core.status.domain.repository

import io.primer.android.payments.core.status.domain.model.AsyncStatus
import kotlinx.coroutines.flow.Flow

internal interface AsyncPaymentMethodStatusRepository {
    fun getAsyncStatus(url: String): Flow<AsyncStatus>
}
