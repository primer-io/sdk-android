package io.primer.android.data.payments.status.repository

import io.primer.android.data.payments.status.exception.AsyncFlowIncompleteException
import io.primer.android.data.payments.status.datasource.RemoteAsyncPaymentMethodStatusDataSource
import io.primer.android.data.payments.status.models.AsyncMethodStatus
import io.primer.android.domain.payments.async.models.AsyncStatus
import io.primer.android.domain.payments.async.repository.AsyncPaymentMethodStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class AsyncPaymentMethodStatusDataRepository(
    private val asyncPaymentMethodStatusDataSource: RemoteAsyncPaymentMethodStatusDataSource
) : AsyncPaymentMethodStatusRepository {

    override fun getAsyncStatus(url: String): Flow<AsyncStatus> {
        return asyncPaymentMethodStatusDataSource.execute(url).map {
            if (it.status != AsyncMethodStatus.COMPLETE) throw AsyncFlowIncompleteException()
            AsyncStatus(it.id)
        }
    }
}
