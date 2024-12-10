package io.primer.android.vouchers.multibanco.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.vouchers.multibanco.implementation.tokenization.data.mapper.MultibancoTokenizationParamsMapper
import io.primer.android.vouchers.multibanco.implementation.tokenization.data.model.MultibancoPaymentInstrumentDataRequest
import io.primer.android.vouchers.multibanco.implementation.tokenization.domain.model.MultibancoPaymentInstrumentParams

internal class MultibancoTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<MultibancoPaymentInstrumentDataRequest>,
    configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: MultibancoTokenizationParamsMapper
) : TokenizationDataRepository<MultibancoPaymentInstrumentParams, MultibancoPaymentInstrumentDataRequest>(
    remoteTokenizationDataSource,
    configurationDataSource,
    tokenizationParamsMapper
)
