package io.primer.android.otp.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.otp.implementation.tokenization.data.mapper.OtpTokenizationParamsMapper
import io.primer.android.otp.implementation.tokenization.data.model.OtpPaymentInstrumentDataRequest
import io.primer.android.otp.implementation.tokenization.domain.model.OtpPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository

internal class OtpTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<OtpPaymentInstrumentDataRequest>,
    configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: OtpTokenizationParamsMapper,
) : TokenizationDataRepository<OtpPaymentInstrumentParams, OtpPaymentInstrumentDataRequest>(
    remoteTokenizationDataSource,
    configurationDataSource,
    tokenizationParamsMapper,
)
