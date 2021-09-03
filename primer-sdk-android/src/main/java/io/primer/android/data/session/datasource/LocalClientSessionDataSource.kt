package io.primer.android.data.session.datasource

import io.primer.android.model.dto.ClientSession
import kotlinx.coroutines.flow.flowOf

internal class LocalClientSessionDataSource {

    private var clientSession: ClientSession? = null

    fun getClientSession() = flowOf(clientSession)

    fun updateClientSession(clientSession: ClientSession) {
        this.clientSession = clientSession
    }
}
