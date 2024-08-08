package io.primer.android.components.data.metadata.phone.datasource

import io.primer.android.components.data.metadata.phone.model.PhoneMetadataResponse
import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient

internal class RemotePhoneMetadataDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<PhoneMetadataResponse, BaseRemoteRequest<String>> {
    override suspend fun execute(input: BaseRemoteRequest<String>) =
        httpClient.suspendGet<PhoneMetadataResponse>(
            "${input.configuration.pciUrl}/phone-number-lookups/${input.data}"
        ).body
}
