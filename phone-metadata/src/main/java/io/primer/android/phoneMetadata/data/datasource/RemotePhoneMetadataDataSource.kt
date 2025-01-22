package io.primer.android.phoneMetadata.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import io.primer.android.phoneMetadata.data.model.PhoneMetadataResponse

internal class RemotePhoneMetadataDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<PhoneMetadataResponse, BaseRemoteHostRequest<String>> {
    override suspend fun execute(input: BaseRemoteHostRequest<String>) =
        httpClient
            .withTimeout(PRIMER_15S_TIMEOUT)
            .suspendGet<PhoneMetadataResponse>(
                "${input.host}/phone-number-lookups/${input.data}",
            ).body
}
