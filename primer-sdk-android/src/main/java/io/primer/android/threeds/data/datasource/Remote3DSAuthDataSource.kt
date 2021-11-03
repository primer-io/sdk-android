package io.primer.android.threeds.data.datasource

import io.primer.android.data.configuration.model.Configuration
import io.primer.android.http.PrimerHttpClient
import io.primer.android.threeds.data.models.BeginAuthRequest
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.PostAuthResponse

internal class Remote3DSAuthDataSource(private val httpClient: PrimerHttpClient) {

    fun get3dsAuthToken(
        configuration: Configuration,
        token: String,
        beginAuthRequest: BeginAuthRequest,
    ) = httpClient.post<BeginAuthRequest, BeginAuthResponse>(
        "${configuration.pciUrl}/3ds/$token/auth",
        beginAuthRequest
    )

    fun continue3dsAuth(
        configuration: Configuration,
        threeDSTokenId: String,
    ) = httpClient.post<Unit, PostAuthResponse>(
        "${configuration.pciUrl}/3ds/$threeDSTokenId/continue",
        Unit
    )
}
