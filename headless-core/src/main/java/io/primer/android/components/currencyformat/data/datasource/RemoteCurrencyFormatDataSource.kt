package io.primer.android.components.currencyformat.data.datasource

import io.primer.android.components.currencyformat.data.models.CurrencyFormatDataResponse
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.network.PrimerHttpClient

internal class RemoteCurrencyFormatDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<CurrencyFormatDataResponse, String> {
    override suspend fun execute(input: String) = httpClient.suspendGet<CurrencyFormatDataResponse>(input).body
}
