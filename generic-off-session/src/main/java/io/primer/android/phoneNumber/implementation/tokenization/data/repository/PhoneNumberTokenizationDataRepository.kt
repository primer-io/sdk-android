package io.primer.android.phoneNumber.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository
import io.primer.android.phoneNumber.implementation.tokenization.data.mapper.PhoneNumberTokenizationParamsMapper
import io.primer.android.phoneNumber.implementation.tokenization.data.model.PhoneNumberPaymentInstrumentDataRequest
import io.primer.android.phoneNumber.implementation.tokenization.domain.model.PhoneNumberPaymentInstrumentParams

internal class PhoneNumberTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<PhoneNumberPaymentInstrumentDataRequest>,
    configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: PhoneNumberTokenizationParamsMapper
) : TokenizationDataRepository<PhoneNumberPaymentInstrumentParams, PhoneNumberPaymentInstrumentDataRequest>(
    remoteTokenizationDataSource,
    configurationDataSource,
    tokenizationParamsMapper
)
