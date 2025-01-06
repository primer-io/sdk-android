package io.primer.android.klarna.implementation.session.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.klarna.implementation.session.data.datasource.RemoteKlarnaCustomerTokenDataSource
import io.primer.android.klarna.implementation.session.data.exception.KlarnaIllegalValueKey
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataRequest
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataResponse
import io.primer.android.klarna.implementation.session.data.models.LocaleDataRequest
import io.primer.android.klarna.implementation.session.domain.models.KlarnaCustomerTokenParam
import io.primer.android.klarna.implementation.session.domain.repository.KlarnaCustomerTokenRepository
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal class KlarnaCustomerTokenDataRepository(
    private val remoteKlarnaCustomerTokenDataSource: RemoteKlarnaCustomerTokenDataSource,
    private val configurationDataSource: CacheConfigurationDataSource,
    private val config: PrimerConfig,
) : KlarnaCustomerTokenRepository {
    override suspend fun createCustomerToken(
        params: KlarnaCustomerTokenParam,
    ): Result<CreateCustomerTokenDataResponse> {
        return runSuspendCatching {
            val recurringPaymentDescription =
                config.settings.paymentMethodOptions.klarnaOptions.recurringPaymentDescription
            val order = requireNotNull(configurationDataSource.get().clientSession.order)
            remoteKlarnaCustomerTokenDataSource.execute(
                BaseRemoteHostRequest(
                    host = configurationDataSource.get().coreUrl,
                    data =
                        CreateCustomerTokenDataRequest(
                            paymentMethodConfigId =
                                requireNotNullCheck(
                                    configurationDataSource.get().paymentMethods
                                        .first { it.type == PaymentMethodType.KLARNA.name }.id,
                                    KlarnaIllegalValueKey.PAYMENT_METHOD_CONFIG_ID,
                                ),
                            sessionId = params.sessionId,
                            authorizationToken = params.authorizationToken,
                            description = recurringPaymentDescription,
                            localeData =
                                LocaleDataRequest(
                                    order.countryCode,
                                    order.currencyCode.orEmpty(),
                                    config.settings.locale.toLanguageTag(),
                                ),
                        ),
                ),
            )
        }.recoverCatching {
            when {
                it is HttpException && it.isClientError() ->
                    throw SessionCreateException(
                        PaymentMethodType.KLARNA.name,
                        it.error.diagnosticsId,
                        it.error.description,
                    )

                else -> throw it
            }
        }
    }
}
