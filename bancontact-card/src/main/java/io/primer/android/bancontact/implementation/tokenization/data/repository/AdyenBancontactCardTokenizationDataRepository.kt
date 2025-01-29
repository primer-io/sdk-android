package io.primer.android.bancontact.implementation.tokenization.data.repository

import io.primer.android.bancontact.implementation.tokenization.data.mapper.AdyenBancontactTokenizationParamsMapper
import io.primer.android.bancontact.implementation.tokenization.data.model.AdyenBancontactPaymentInstrumentDataRequest
import io.primer.android.bancontact.implementation.tokenization.domain.model.AdyenBancontactPaymentInstrumentParams
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository

internal class AdyenBancontactCardTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<AdyenBancontactPaymentInstrumentDataRequest>,
    configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: AdyenBancontactTokenizationParamsMapper,
) : TokenizationDataRepository<AdyenBancontactPaymentInstrumentParams, AdyenBancontactPaymentInstrumentDataRequest>(
    remoteTokenizationDataSource,
    configurationDataSource,
    tokenizationParamsMapper,
)
