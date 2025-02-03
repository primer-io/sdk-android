package io.primer.android.stripe.ach.implementation.tokenization.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository
import io.primer.android.stripe.ach.implementation.tokenization.data.model.StripeAchPaymentInstrumentDataRequest
import io.primer.android.stripe.ach.implementation.tokenization.domain.model.StripeAchPaymentInstrumentParams

internal class StripeAchTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<StripeAchPaymentInstrumentDataRequest>,
    localConfigurationDataSource: CacheConfigurationDataSource,
    tokenizationParamsMapper: TokenizationParamsMapper<StripeAchPaymentInstrumentParams, StripeAchPaymentInstrumentDataRequest>,
) : TokenizationDataRepository<StripeAchPaymentInstrumentParams, StripeAchPaymentInstrumentDataRequest>(
    remoteTokenizationDataSource,
    localConfigurationDataSource,
    tokenizationParamsMapper,
)
