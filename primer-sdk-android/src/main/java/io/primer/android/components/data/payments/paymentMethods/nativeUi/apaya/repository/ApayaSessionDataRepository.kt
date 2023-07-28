package io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.datasource.RemoteApayaDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.models.toApayaSession
import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.models.toCreateSessionRequest
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.payments.apaya.models.ApayaSession
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import io.primer.android.domain.payments.apaya.repository.ApayaSessionRepository
import io.primer.android.extensions.doOnError
import io.primer.android.http.exception.HttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal class ApayaSessionDataRepository(
    private val remoteApayaDataSource: RemoteApayaDataSource,
    private val configurationDataSource: LocalConfigurationDataSource,
    private val settings: PrimerSettings
) : ApayaSessionRepository {

    override fun createClientSession(params: ApayaSessionParams): Flow<ApayaSession> {
        val paymentMethodConfig =
            configurationDataSource.getConfiguration().paymentMethods
                .first { it.type == PaymentMethodType.APAYA.name }
        return configurationDataSource.get().flatMapLatest {
            remoteApayaDataSource.execute(
                BaseRemoteRequest(it, params.toCreateSessionRequest())
            ).map {
                it.toApayaSession(
                    settings.paymentMethodOptions.apayaOptions.webViewTitle
                        ?: paymentMethodConfig.name
                )
            }
        }.doOnError {
            when {
                it is HttpException && it.isClientError() ->
                    throw SessionCreateException(PaymentMethodType.APAYA, it.error.diagnosticsId)
                else -> throw it
            }
        }
    }
}
