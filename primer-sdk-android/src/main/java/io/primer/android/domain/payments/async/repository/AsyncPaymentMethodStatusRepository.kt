package io.primer.android.domain.payments.async.repository

import io.primer.android.domain.payments.async.models.AsyncStatus
import kotlinx.coroutines.flow.Flow

internal interface AsyncPaymentMethodStatusRepository {

    fun getAsyncStatus(url: String): Flow<AsyncStatus>
}
