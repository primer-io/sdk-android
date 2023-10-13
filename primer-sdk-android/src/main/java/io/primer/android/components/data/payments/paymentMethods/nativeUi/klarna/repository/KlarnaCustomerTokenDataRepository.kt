package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteKlarnaCustomerTokenDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.exception.KlarnaIllegalValueKey
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.LocaleDataRequest
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaCustomerTokenRepository
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.extensions.doOnError
import io.primer.android.http.exception.HttpException
import kotlinx.coroutines.flow.Flow

internal class KlarnaCustomerTokenDataRepository(
    private val remoteKlarnaCustomerTokenDataSource: RemoteKlarnaCustomerTokenDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val config: PrimerConfig
) : KlarnaCustomerTokenRepository {
    override fun createCustomerToken(params: KlarnaCustomerTokenParam):
        Flow<CreateCustomerTokenDataResponse> {
        return remoteKlarnaCustomerTokenDataSource.execute(
            BaseRemoteRequest(
                localConfigurationDataSource.getConfiguration(),
                CreateCustomerTokenDataRequest(
                    requireNotNullCheck(
                        localConfigurationDataSource.getConfiguration().paymentMethods
                            .first { it.type == PaymentMethodType.KLARNA.name }.id,
                        KlarnaIllegalValueKey.PAYMENT_METHOD_CONFIG_ID
                    ),
                    params.sessionId,
                    params.authorizationToken,
                    config.settings.paymentMethodOptions.klarnaOptions.recurringPaymentDescription,
                    LocaleDataRequest(
                        config.settings.order.countryCode,
                        config.settings.currency,
                        config.settings.locale.toLanguageTag()
                    )
                )
            )
        ).doOnError {
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
}
