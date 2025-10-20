package io.primer.android.components.currencyformat.data

import io.primer.android.components.currencyformat.data.datasource.LocalCurrencyFormatDataSource
import io.primer.android.components.currencyformat.data.datasource.RemoteCurrencyFormatDataSource
import io.primer.android.components.currencyformat.domain.repository.CurrencyFormatRepository
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.extensions.runSuspendCatching
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeoutException

internal class CurrencyFormatDataRepository(
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val localDataSource: LocalCurrencyFormatDataSource,
    private val remoteDataSource: RemoteCurrencyFormatDataSource,
) : CurrencyFormatRepository {
    override suspend fun fetchCurrencyFormats() =
        runSuspendCatching {
            val assetUrl = configurationDataSource.get().assetsUrl
            val url = "$assetUrl/currency-information/v1/data.json"

            val response = withTimeoutOrNull(timeMillis = TIMEOUT_IN_MILLIS) { remoteDataSource.execute(url) }
                ?: throw TimeoutException("Request timed out")
            localDataSource.update(response)
        }

    override fun getCurrencyFormats() = localDataSource.get().data

    private companion object {

        const val TIMEOUT_IN_MILLIS = 500L
    }
}
