package io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.mapper.RetailOutletsTokenizationParamsMapper
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.model.RetailOutletsPaymentInstrumentDataRequest
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.domain.model.RetailOutletsPaymentInstrumentParams

internal class RetailerOutletsTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<RetailOutletsPaymentInstrumentDataRequest>,
    configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: RetailOutletsTokenizationParamsMapper
) : TokenizationDataRepository<RetailOutletsPaymentInstrumentParams, RetailOutletsPaymentInstrumentDataRequest>(
    remoteTokenizationDataSource,
    configurationDataSource,
    tokenizationParamsMapper
)
