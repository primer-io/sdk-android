package io.primer.android.data.tokenization.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.tokenization.datasource.RemoteTokenizationDataSource
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.data.tokenization.models.toTokenizationRequest
import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.repository.TokenizationRepository
import kotlinx.coroutines.flow.Flow

internal class TokenizationDataRepository(
    private val remoteTokenizationDataSource: RemoteTokenizationDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : TokenizationRepository {

    override fun tokenize(params: TokenizationParams): Flow<PaymentMethodTokenInternal> {
        return remoteTokenizationDataSource.executeV1(
            BaseRemoteRequest(
                localConfigurationDataSource.getConfiguration(),
                params.toTokenizationRequest()
            )
        )
    }

    override fun tokenize(params: TokenizationParamsV2): Flow<PaymentMethodTokenInternal> {
        return remoteTokenizationDataSource.execute(
            BaseRemoteRequest(
                localConfigurationDataSource.getConfiguration(),
                params.toTokenizationRequest()
            )
        )
    }
}
