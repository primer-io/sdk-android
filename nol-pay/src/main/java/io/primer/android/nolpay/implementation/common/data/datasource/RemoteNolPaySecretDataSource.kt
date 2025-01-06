package io.primer.android.nolpay.implementation.common.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.nolpay.implementation.common.data.model.NolPaySecretDataRequest
import io.primer.android.nolpay.implementation.common.data.model.NolPaySecretDataResponse

internal class RemoteNolPaySecretDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseSuspendDataSource<NolPaySecretDataResponse, BaseRemoteHostRequest<NolPaySecretDataRequest>> {
    override suspend fun execute(input: BaseRemoteHostRequest<NolPaySecretDataRequest>) =
        primerHttpClient.suspendPost<NolPaySecretDataRequest, NolPaySecretDataResponse>(
            url = "${input.host}/nol-pay/sdk-secrets",
            request = input.data,
        ).body
}
