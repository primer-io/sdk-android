package io.primer.android.sandboxProcessor.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository
import io.primer.android.sandboxProcessor.implementation.tokenization.data.mapper.SandboxProcessorTokenizationParamsMapper
import io.primer.android.sandboxProcessor.implementation.tokenization.data.model.SandboxProcessorPaymentInstrumentDataRequest
import io.primer.android.sandboxProcessor.implementation.tokenization.domain.model.SandboxProcessorPaymentInstrumentParams

internal class SandboxProcessorTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<SandboxProcessorPaymentInstrumentDataRequest>,
    configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: SandboxProcessorTokenizationParamsMapper,
) : TokenizationDataRepository<SandboxProcessorPaymentInstrumentParams, SandboxProcessorPaymentInstrumentDataRequest>(
        remoteTokenizationDataSource,
        configurationDataSource,
        tokenizationParamsMapper,
    )
