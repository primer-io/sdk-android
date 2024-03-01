package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteFinalizeKlarnaSessionDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.exception.KlarnaIllegalValueKey
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.FinalizeKlarnaSessionDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.FinalizeKlarnaSessionDataResponse
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.FinalizeKlarnaSessionRepository
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.extensions.runSuspendCatching
import io.primer.android.http.exception.HttpException

internal class FinalizeKlarnaSessionDataRepository(
    private val remoteFinalizeKlarnaSessionDataSource: RemoteFinalizeKlarnaSessionDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : FinalizeKlarnaSessionRepository {
    override suspend fun finalize(params: KlarnaCustomerTokenParam):
        Result<FinalizeKlarnaSessionDataResponse> = runSuspendCatching {
        remoteFinalizeKlarnaSessionDataSource.execute(
            BaseRemoteRequest(
                localConfigurationDataSource.getConfiguration(),
                FinalizeKlarnaSessionDataRequest(
                    requireNotNullCheck(
                        localConfigurationDataSource.getConfiguration().paymentMethods
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
                    PaymentMethodType.KLARNA,
                    it.error.diagnosticsId,
                    it.error.description
                )

            else -> throw it
        }
    }
}
