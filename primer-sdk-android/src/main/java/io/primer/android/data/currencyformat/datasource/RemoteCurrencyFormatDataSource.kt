package io.primer.android.data.currencyformat.datasource

import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.currencyformat.models.CurrencyFormatDataResponse
import io.primer.android.http.PrimerHttpClient

internal class RemoteCurrencyFormatDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<CurrencyFormatDataResponse, String> {
    override suspend fun execute(input: String) =
        httpClient.suspendGet<CurrencyFormatDataResponse>(input).body
}
