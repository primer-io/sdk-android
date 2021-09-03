package io.primer.android.data.payments.apaya.repository

import io.primer.android.data.payments.apaya.datasource.RemoteApayaDataSource
import io.primer.android.data.payments.apaya.models.toApayaSession
import io.primer.android.data.session.datasource.LocalClientSessionDataSource
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import io.primer.android.domain.payments.apaya.models.toCreateSessionRequest
import io.primer.android.domain.payments.apaya.repository.ApayaRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal class ApayaDataRepository(
    private val remoteApayaDataSource: RemoteApayaDataSource,
    private var clientSessionDataSource: LocalClientSessionDataSource,
) : ApayaRepository {

    override fun createClientSession(params: ApayaSessionParams) =
        clientSessionDataSource.getClientSession().flatMapLatest {
            remoteApayaDataSource.createSession(it, params.toCreateSessionRequest())
                .map { it.toApayaSession() }
        }
}
