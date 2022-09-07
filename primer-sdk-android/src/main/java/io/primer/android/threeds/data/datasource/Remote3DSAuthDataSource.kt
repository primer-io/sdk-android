package io.primer.android.threeds.data.datasource

import io.primer.android.core.data.models.EmptyDataRequest
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.di.ApiVersion
import io.primer.android.di.SDK_API_VERSION_HEADER
import io.primer.android.http.PrimerHttpClient
import io.primer.android.threeds.data.models.BeginAuthDataRequest
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.PostAuthResponse

internal class Remote3DSAuthDataSource(private val httpClient: PrimerHttpClient) {

    fun get3dsAuthToken(
        configuration: ConfigurationData,
        token: String,
        beginAuthRequest: BeginAuthDataRequest,
    ) = httpClient.post<BeginAuthDataRequest, BeginAuthResponse>(
        "${configuration.pciUrl}/3ds/$token/auth",
        beginAuthRequest,
        mapOf(SDK_API_VERSION_HEADER to ApiVersion.THREE_DS_VERSION.version)
    )

    fun continue3dsAuth(
        configuration: ConfigurationData,
        threeDSTokenId: String,
    ) = httpClient.post<EmptyDataRequest, PostAuthResponse>(
        "${configuration.pciUrl}/3ds/$threeDSTokenId/continue",
        EmptyDataRequest(),
        mapOf(SDK_API_VERSION_HEADER to ApiVersion.THREE_DS_VERSION.version)
    )
}
