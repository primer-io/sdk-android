package io.primer.android.data.payments.apaya.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.payments.apaya.datasource.RemoteApayaDataSource
import io.primer.android.data.payments.apaya.models.toApayaSession
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.payments.apaya.models.toCreateSessionRequest
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import io.primer.android.domain.payments.apaya.repository.ApayaRepository
import io.primer.android.extensions.doOnError
import io.primer.android.http.exception.HttpException
import io.primer.android.model.dto.PaymentMethodType
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal class ApayaDataRepository(
    private val remoteApayaDataSource: RemoteApayaDataSource,
    private var configurationDataSource: LocalConfigurationDataSource,
) : ApayaRepository {

    override fun createClientSession(params: ApayaSessionParams) =
        configurationDataSource.get().flatMapLatest {
            remoteApayaDataSource.execute(
                BaseRemoteRequest(it, params.toCreateSessionRequest())
            ).map { it.toApayaSession() }
        }.doOnError {
            when {
                it is HttpException && it.isClientError() ->
                    throw SessionCreateException(PaymentMethodType.APAYA, it.error.diagnosticsId)
                else -> throw it
            }
        }
}
