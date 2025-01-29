package io.primer.android.nolpay.implementation.paymentCard.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.nolpay.implementation.paymentCard.tokenization.data.mapper.NolPayTokenizationParamsMapper
import io.primer.android.nolpay.implementation.paymentCard.tokenization.data.model.NolPayPaymentInstrumentDataRequest
import io.primer.android.nolpay.implementation.paymentCard.tokenization.domain.model.NolPayPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository

internal class NolPayTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<NolPayPaymentInstrumentDataRequest>,
    configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: NolPayTokenizationParamsMapper,
) : TokenizationDataRepository<NolPayPaymentInstrumentParams, NolPayPaymentInstrumentDataRequest>(
    remoteTokenizationDataSource,
    configurationDataSource,
    tokenizationParamsMapper,
)
