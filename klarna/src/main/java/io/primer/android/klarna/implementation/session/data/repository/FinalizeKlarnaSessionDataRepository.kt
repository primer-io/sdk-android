package io.primer.android.klarna.implementation.session.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.klarna.implementation.session.domain.models.KlarnaCustomerTokenParam
import io.primer.android.klarna.implementation.session.domain.repository.FinalizeKlarnaSessionRepository
import io.primer.android.klarna.implementation.session.data.datasource.RemoteFinalizeKlarnaSessionDataSource
import io.primer.android.klarna.implementation.session.data.exception.KlarnaIllegalValueKey
import io.primer.android.klarna.implementation.session.data.models.FinalizeKlarnaSessionDataRequest
import io.primer.android.klarna.implementation.session.data.models.FinalizeKlarnaSessionDataResponse

internal class FinalizeKlarnaSessionDataRepository(
    private val remoteFinalizeKlarnaSessionDataSource: RemoteFinalizeKlarnaSessionDataSource,
    private val configurationDataSource: CacheConfigurationDataSource
) : FinalizeKlarnaSessionRepository {
    override suspend fun finalize(params: KlarnaCustomerTokenParam):
        Result<FinalizeKlarnaSessionDataResponse> = runSuspendCatching {
        remoteFinalizeKlarnaSessionDataSource.execute(
            BaseRemoteHostRequest(
                configurationDataSource.get().coreUrl,
                FinalizeKlarnaSessionDataRequest(
                    requireNotNullCheck(
                        configurationDataSource.get().paymentMethods
                            .first { it.type == PaymentMethodType.KLARNA.name }.id,
                        KlarnaIllegalValueKey.PAYMENT_METHOD_CONFIG_ID
                    ),
                    params.sessionId
                )
            )
        )
    }.recoverCatching {
        when {
            it is HttpException && it.isClientError() ->
                throw SessionCreateException(
                    PaymentMethodType.KLARNA.name,
                    it.error.diagnosticsId,
                    it.error.description
                )

            else -> throw it
        }
    }
}
