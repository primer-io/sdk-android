package io.primer.android.googlepay.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.googlepay.implementation.tokenization.data.mapper.GooglePayTokenizationParamsMapper
import io.primer.android.googlepay.implementation.tokenization.data.model.GooglePayPaymentInstrumentDataRequest
import io.primer.android.googlepay.implementation.tokenization.domain.model.GooglePayPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository

internal class GooglePayTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<GooglePayPaymentInstrumentDataRequest>,
    configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: GooglePayTokenizationParamsMapper,
) : TokenizationDataRepository<GooglePayPaymentInstrumentParams, GooglePayPaymentInstrumentDataRequest>(
    remoteTokenizationDataSource,
    configurationDataSource,
    tokenizationParamsMapper,
)
