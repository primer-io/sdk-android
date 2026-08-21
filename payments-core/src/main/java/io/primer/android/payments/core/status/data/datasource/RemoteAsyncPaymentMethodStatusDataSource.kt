package io.primer.android.payments.core.status.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.retry.RetryPolicy
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
                // retryOn omitted: the default of retrying every 5xx applies alongside transient failures.
                retryPolicy = RetryPolicy(maxAttempts = MAX_ATTEMPTS),
            ).body

    private companion object {
        /** One initial attempt plus three retries (the legacy 1 + maxRetries = 3 behavior). */
        const val MAX_ATTEMPTS = 4
    }
}
