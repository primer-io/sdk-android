package io.primer.android.card.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.card.implementation.tokenization.data.mapper.CardTokenizationParamsMapper
import io.primer.android.card.implementation.tokenization.data.model.CardPaymentInstrumentDataRequest
import io.primer.android.card.implementation.tokenization.domain.model.CardPaymentInstrumentParams

internal class CardTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<CardPaymentInstrumentDataRequest>,
    configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: CardTokenizationParamsMapper
) : TokenizationDataRepository<CardPaymentInstrumentParams, CardPaymentInstrumentDataRequest>(
    remoteTokenizationDataSource,
    configurationDataSource,
    tokenizationParamsMapper
)
