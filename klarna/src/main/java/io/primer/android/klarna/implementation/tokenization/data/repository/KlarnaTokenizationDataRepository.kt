package io.primer.android.klarna.implementation.tokenization.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.klarna.implementation.tokenization.data.mapper.KlarnaTokenizationParamsMapper
import io.primer.android.klarna.implementation.tokenization.data.model.KlarnaPaymentInstrumentDataRequest
import io.primer.android.klarna.implementation.tokenization.domain.model.KlarnaPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository

internal class KlarnaTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<KlarnaPaymentInstrumentDataRequest>,
    localConfigurationDataSource: CacheConfigurationDataSource,
    tokenizationParamsMapper: KlarnaTokenizationParamsMapper,
) : TokenizationDataRepository<KlarnaPaymentInstrumentParams, KlarnaPaymentInstrumentDataRequest>(
    remoteTokenizationDataSource,
    localConfigurationDataSource,
    tokenizationParamsMapper,
)
