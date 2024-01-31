package io.primer.android.data.currencyformat.repository

import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.currencyformat.datasource.LocalCurrencyFormatDataSource
import io.primer.android.data.currencyformat.datasource.RemoteCurrencyFormatDataSource
import io.primer.android.domain.currencyformat.repository.CurrencyFormatRepository
import io.primer.android.extensions.runSuspendCatching

internal class CurrencyFormatDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val localDataSource: LocalCurrencyFormatDataSource,
    private val remoteDataSource: RemoteCurrencyFormatDataSource
) : CurrencyFormatRepository {
    override suspend fun fetchCurrencyFormats() =
        runSuspendCatching {
            val assetUrl = localConfigurationDataSource.getConfiguration().assetsUrl
            val url = "$assetUrl/currency-information/v1/data.json"

            val response = remoteDataSource.execute(url)
            localDataSource.update(response)
        }

    override fun getCurrencyFormats() = localDataSource.get().data
}
