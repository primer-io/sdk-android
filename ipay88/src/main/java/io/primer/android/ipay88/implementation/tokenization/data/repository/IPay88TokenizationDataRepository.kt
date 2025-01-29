package io.primer.android.ipay88.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.ipay88.implementation.tokenization.data.mapper.IPay88TokenizationParamsMapper
import io.primer.android.ipay88.implementation.tokenization.data.model.IPay88PaymentInstrumentDataRequest
import io.primer.android.ipay88.implementation.tokenization.domain.model.IPay88PaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository

internal class IPay88TokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<IPay88PaymentInstrumentDataRequest>,
    cacheDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: IPay88TokenizationParamsMapper,
) : TokenizationDataRepository<IPay88PaymentInstrumentParams, IPay88PaymentInstrumentDataRequest>(
    remoteTokenizationDataSource = remoteTokenizationDataSource,
    cacheDataSource = cacheDataSource,
    tokenizationParamsMapper = tokenizationParamsMapper,
)
