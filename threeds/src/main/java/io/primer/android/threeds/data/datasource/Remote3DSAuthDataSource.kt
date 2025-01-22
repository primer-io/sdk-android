package io.primer.android.threeds.data.datasource

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import io.primer.android.threeds.data.models.auth.BeginAuthDataRequest
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.postAuth.BaseContinueAuthDataRequest
import io.primer.android.threeds.data.models.postAuth.PostAuthResponse

internal class Remote3DSAuthDataSource(
    private val httpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) {
    suspend fun get3dsAuthToken(
        configuration: ConfigurationData,
        paymentMethodToken: String,
        beginAuthRequest: BeginAuthDataRequest,
    ) = httpClient.withTimeout(PRIMER_15S_TIMEOUT)
        .suspendPost<BeginAuthDataRequest, BeginAuthResponse>(
            url = "${configuration.pciUrl}/3ds/$paymentMethodToken/auth",
            request = beginAuthRequest,
            headers = apiVersion().toHeaderMap(),
        )

    suspend fun continue3dsAuth(
        configuration: ConfigurationData,
        paymentMethodToken: String,
        continueAuthDataRequest: BaseContinueAuthDataRequest,
    ) = httpClient.withTimeout(PRIMER_15S_TIMEOUT)
        .suspendPost<BaseContinueAuthDataRequest, PostAuthResponse>(
            url = "${configuration.pciUrl}/3ds/$paymentMethodToken/continue",
            request = continueAuthDataRequest,
            headers = apiVersion().toHeaderMap(),
        )
}
