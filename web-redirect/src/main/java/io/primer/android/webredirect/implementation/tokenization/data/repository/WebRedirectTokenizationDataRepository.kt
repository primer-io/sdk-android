package io.primer.android.webredirect.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository
import io.primer.android.webredirect.implementation.tokenization.data.mapper.WebRedirectTokenizationParamsMapper
import io.primer.android.webredirect.implementation.tokenization.data.model.WebRedirectPaymentInstrumentDataRequest
import io.primer.android.webredirect.implementation.tokenization.domain.model.WebRedirectPaymentInstrumentParams
import io.primer.android.core.data.datasource.BaseCacheDataSource

internal class WebRedirectTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<WebRedirectPaymentInstrumentDataRequest>,
    cacheDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: WebRedirectTokenizationParamsMapper
) : TokenizationDataRepository<WebRedirectPaymentInstrumentParams, WebRedirectPaymentInstrumentDataRequest>(
    remoteTokenizationDataSource = remoteTokenizationDataSource,
    cacheDataSource = cacheDataSource,
    tokenizationParamsMapper = tokenizationParamsMapper
)
