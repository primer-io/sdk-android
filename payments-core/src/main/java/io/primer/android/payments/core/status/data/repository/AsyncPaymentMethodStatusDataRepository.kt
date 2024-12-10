package io.primer.android.payments.core.status.data.repository

import io.primer.android.payments.core.status.data.datasource.RemoteAsyncPaymentMethodStatusDataSource
import io.primer.android.payments.core.errors.data.exception.AsyncFlowIncompleteException
import io.primer.android.payments.core.status.data.models.AsyncMethodStatus
import io.primer.android.payments.core.status.domain.model.AsyncStatus
import io.primer.android.payments.core.status.domain.repository.AsyncPaymentMethodStatusRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry

internal class AsyncPaymentMethodStatusDataRepository(
    private val asyncPaymentMethodStatusDataSource: RemoteAsyncPaymentMethodStatusDataSource
) : AsyncPaymentMethodStatusRepository {

    override fun getAsyncStatus(url: String): Flow<AsyncStatus> {
        return asyncPaymentMethodStatusDataSource.execute(url).map { statusResponse ->
            if (statusResponse.status != AsyncMethodStatus.COMPLETE) throw AsyncFlowIncompleteException()
            AsyncStatus(resumeToken = statusResponse.id)
        }.retry {
            (it is AsyncFlowIncompleteException).also { retrying ->
                if (retrying) delay(POLL_DELAY)
            }
        }
    }

    private companion object {

        const val POLL_DELAY = 1000L
    }
}
