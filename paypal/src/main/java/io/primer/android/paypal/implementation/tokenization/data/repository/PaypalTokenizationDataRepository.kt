package io.primer.android.paypal.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.paypal.implementation.tokenization.data.mapper.PaypalTokenizationParamsMapper
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalPaymentInstrumentDataRequest
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalPaymentInstrumentParams

internal class PaypalTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<PaypalPaymentInstrumentDataRequest>,
    cacheDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: PaypalTokenizationParamsMapper
) : TokenizationDataRepository<PaypalPaymentInstrumentParams, PaypalPaymentInstrumentDataRequest>(
    remoteTokenizationDataSource = remoteTokenizationDataSource,
    cacheDataSource = cacheDataSource,
    tokenizationParamsMapper = tokenizationParamsMapper
)
