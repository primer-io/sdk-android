package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteKlarnaSessionDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.exception.KlarnaIllegalValueKey
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateSessionDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.LocaleDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.toKlarnaSession
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaSessionRepository
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.extensions.doOnError
import io.primer.android.http.exception.HttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

internal class KlarnaSessionDataRepository(
    private val klarnaSessionDataSource: RemoteKlarnaSessionDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val config: PrimerConfig
) : KlarnaSessionRepository {
    override fun createSession(): Flow<KlarnaSession> {
        val paymentMethodConfig =
            localConfigurationDataSource.getConfiguration().paymentMethods
                .first { it.type == PaymentMethodType.KLARNA.name }
        return klarnaSessionDataSource.execute(
            BaseRemoteRequest(
                localConfigurationDataSource.getConfiguration(),
                CreateSessionDataRequest(
                    requireNotNullCheck(
                        paymentMethodConfig.id,
                        KlarnaIllegalValueKey.PAYMENT_METHOD_CONFIG_ID
                    ),
                    "RECURRING_PAYMENT",
                    config.settings.paymentMethodOptions.klarnaOptions.recurringPaymentDescription,
                    LocaleDataRequest(
                        config.settings.order.countryCode,
                        config.settings.currency,
                        config.settings.locale.toLanguageTag(),
                    )
                )
            )
        ).mapLatest {
            it.toKlarnaSession(
                config.settings.paymentMethodOptions.klarnaOptions.webViewTitle
                    ?: paymentMethodConfig.name
            )
        }.doOnError {
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
