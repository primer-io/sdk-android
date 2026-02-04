package io.primer.android.payments.core.status.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.retry.RetryConfig
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT
import io.primer.android.payments.core.status.data.models.AsyncPaymentMethodStatusDataResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal class RemoteAsyncPaymentMethodStatusDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<AsyncPaymentMethodStatusDataResponse, String> {
    override suspend fun execute(input: String) =
        primerHttpClient.withTimeout(PRIMER_60S_TIMEOUT)
            .retrySuspendGet<AsyncPaymentMethodStatusDataResponse>(
                url = input,
                retryConfig = RetryConfig(
                    enabled = true,
                    retry500Errors = true,
                    maxRetries = MAX_RETRY_COUNT,
                ),
            ).body

    private companion object {
        const val MAX_RETRY_COUNT = 3
    }
}
