package io.primer.android.qrcode.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.repository.TokenizationDataRepository
import io.primer.android.qrcode.implementation.tokenization.data.mapper.QrCodeTokenizationParamsMapper
import io.primer.android.qrcode.implementation.tokenization.data.model.QrCodePaymentInstrumentDataRequest
import io.primer.android.qrcode.implementation.tokenization.domain.model.QrCodePaymentInstrumentParams

internal class QrCodeTokenizationDataRepository(
    remoteTokenizationDataSource: BaseRemoteTokenizationDataSource<QrCodePaymentInstrumentDataRequest>,
    configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    tokenizationParamsMapper: QrCodeTokenizationParamsMapper,
) : TokenizationDataRepository<QrCodePaymentInstrumentParams, QrCodePaymentInstrumentDataRequest>(
    remoteTokenizationDataSource,
    configurationDataSource,
    tokenizationParamsMapper,
)
