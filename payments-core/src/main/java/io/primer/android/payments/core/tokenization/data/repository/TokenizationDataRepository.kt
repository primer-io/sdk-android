package io.primer.android.payments.core.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.BasePaymentInstrumentDataRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.extensions.runSuspendCatching

abstract class TokenizationDataRepository<T : BasePaymentInstrumentParams,
    U : BasePaymentInstrumentDataRequest>(
    private val remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<U>,
    private val cacheDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val tokenizationParamsMapper: TokenizationParamsMapper<T, U>
) : TokenizationRepository<T> {

    override suspend fun tokenize(params: TokenizationParams<T>) = runSuspendCatching {
        remoteTokenizationDataSource.execute(
            BaseRemoteHostRequest(
                host = cacheDataSource.get().pciUrl,
                data = tokenizationParamsMapper.map(params)
            )
        )
    }
}
