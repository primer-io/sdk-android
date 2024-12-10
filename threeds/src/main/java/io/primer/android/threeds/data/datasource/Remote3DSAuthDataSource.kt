package io.primer.android.threeds.data.datasource

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.Constants
import io.primer.android.threeds.data.models.auth.BeginAuthDataRequest
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.postAuth.BaseContinueAuthDataRequest
import io.primer.android.threeds.data.models.postAuth.PostAuthResponse

internal class Remote3DSAuthDataSource(private val httpClient: PrimerHttpClient) {

    suspend fun get3dsAuthToken(
        configuration: ConfigurationData,
        paymentMethodToken: String,
        beginAuthRequest: BeginAuthDataRequest
    ) = httpClient.suspendPost<BeginAuthDataRequest, BeginAuthResponse>(
        url = "${configuration.pciUrl}/3ds/$paymentMethodToken/auth",
        request = beginAuthRequest,
        headers = mapOf(Constants.SDK_API_VERSION_HEADER to THREE_DS_VERSION)
    )

    suspend fun continue3dsAuth(
        configuration: ConfigurationData,
        paymentMethodToken: String,
        continueAuthDataRequest: BaseContinueAuthDataRequest
    ) = httpClient.suspendPost<BaseContinueAuthDataRequest, PostAuthResponse>(
        url = "${configuration.pciUrl}/3ds/$paymentMethodToken/continue",
        request = continueAuthDataRequest,
        headers = mapOf(Constants.SDK_API_VERSION_HEADER to THREE_DS_VERSION)
    )

    private companion object {

        const val THREE_DS_VERSION = "2.1"
    }
}
