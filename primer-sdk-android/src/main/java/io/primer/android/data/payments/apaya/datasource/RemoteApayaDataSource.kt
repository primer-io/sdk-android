package io.primer.android.data.payments.apaya.datasource

import io.primer.android.data.payments.apaya.models.CreateSessionRequest
import io.primer.android.data.payments.apaya.models.CreateSessionResponse
import io.primer.android.http.PrimerHttpClient
import io.primer.android.model.dto.ClientSession

internal class RemoteApayaDataSource(
    private val primerHttpClient: PrimerHttpClient,
) {

    fun createSession(clientSession: ClientSession?, createSessionRequest: CreateSessionRequest) =
        primerHttpClient.post<CreateSessionRequest, CreateSessionResponse>(
            "${clientSession?.coreUrl}/session-token",
            createSessionRequest
        )
}
