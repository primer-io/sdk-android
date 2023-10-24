package io.primer.android.components.data.metadata.phone.datasource

import android.net.Uri
import io.primer.android.components.data.metadata.phone.model.PhoneMetadataResponse
import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient
import io.primer.android.utils.buildWithQueryParams

internal class RemotePhoneMetadataDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<PhoneMetadataResponse, BaseRemoteRequest<String>> {
    override suspend fun execute(input: BaseRemoteRequest<String>) =
        httpClient.suspendGet<PhoneMetadataResponse>(
            Uri.parse("${input.configuration.pciUrl}/phone-numbers-metadata")
                .buildWithQueryParams(mapOf(PHONE_NUMBER_QUERY_PARAM to input.data))
        )

    private companion object {

        const val PHONE_NUMBER_QUERY_PARAM = "phoneNumber"
    }
}
