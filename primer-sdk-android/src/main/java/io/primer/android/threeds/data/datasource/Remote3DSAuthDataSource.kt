package io.primer.android.threeds.data.datasource

import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.di.ApiVersion
import io.primer.android.di.NetworkContainer.Companion.SDK_API_VERSION_HEADER
import io.primer.android.http.PrimerHttpClient
import io.primer.android.threeds.data.models.auth.BeginAuthDataRequest
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.postAuth.BaseContinueAuthDataRequest
import io.primer.android.threeds.data.models.postAuth.PostAuthResponse

internal class Remote3DSAuthDataSource(private val httpClient: PrimerHttpClient) {

    fun get3dsAuthToken(
        configuration: ConfigurationData,
        paymentMethodToken: String,
        beginAuthRequest: BeginAuthDataRequest
    ) = httpClient.post<BeginAuthDataRequest, BeginAuthResponse>(
        "${configuration.pciUrl}/3ds/$paymentMethodToken/auth",
        beginAuthRequest,
        mapOf(SDK_API_VERSION_HEADER to ApiVersion.THREE_DS_VERSION.version)
    )

    fun continue3dsAuth(
        configuration: ConfigurationData,
        paymentMethodToken: String,
        continueAuthDataRequest: BaseContinueAuthDataRequest
    ) = httpClient.post<BaseContinueAuthDataRequest, PostAuthResponse>(
        "${configuration.pciUrl}/3ds/$paymentMethodToken/continue",
        continueAuthDataRequest,
        mapOf(SDK_API_VERSION_HEADER to ApiVersion.THREE_DS_VERSION.version)
    )
}
