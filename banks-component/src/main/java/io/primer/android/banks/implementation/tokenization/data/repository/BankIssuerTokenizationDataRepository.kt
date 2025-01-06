package io.primer.android.banks.implementation.tokenization.data.repository

import io.primer.android.banks.implementation.tokenization.data.mapper.BankIssuerTokenizationParamsMapper
import io.primer.android.banks.implementation.tokenization.data.model.BankIssuerPaymentInstrumentDataRequest
import io.primer.android.banks.implementation.tokenization.domain.model.BankIssuerPaymentInstrumentParams
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository

internal class BankIssuerTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<BankIssuerPaymentInstrumentDataRequest>,
    cacheDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: BankIssuerTokenizationParamsMapper,
) : TokenizationDataRepository<BankIssuerPaymentInstrumentParams, BankIssuerPaymentInstrumentDataRequest>(
        remoteTokenizationDataSource = remoteTokenizationDataSource,
        cacheDataSource = cacheDataSource,
        tokenizationParamsMapper = tokenizationParamsMapper,
    )
