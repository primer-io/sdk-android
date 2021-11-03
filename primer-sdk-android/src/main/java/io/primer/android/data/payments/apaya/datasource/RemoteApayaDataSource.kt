package io.primer.android.data.payments.apaya.datasource

import io.primer.android.data.payments.apaya.models.CreateSessionRequest
import io.primer.android.data.payments.apaya.models.CreateSessionResponse
import io.primer.android.http.PrimerHttpClient
import io.primer.android.data.configuration.model.Configuration

internal class RemoteApayaDataSource(
    private val primerHttpClient: PrimerHttpClient,
) {

    fun createSession(
        configuration: Configuration?,
        createSessionRequest: CreateSessionRequest
    ) =
        primerHttpClient.post<CreateSessionRequest, CreateSessionResponse>(
            "${configuration?.coreUrl}/session-token",
            createSessionRequest
        )
}
