package io.primer.android.data.payments.async.repository

import io.primer.android.data.payments.async.exception.AsyncFlowIncompleteException
import io.primer.android.data.payments.async.datasource.RemoteAsyncPaymentMethodStatusDataSource
import io.primer.android.data.payments.async.models.AsyncMethodStatus
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
