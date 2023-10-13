package io.primer.android.components.data.payments.paymentMethods.nolpay.datasource

import io.primer.android.components.data.payments.paymentMethods.nolpay.model.NolPaySecretDataRequest
import io.primer.android.components.data.payments.paymentMethods.nolpay.model.NolPaySecretDataResponse
import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient

internal class RemoteNolPaySecretDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseSuspendDataSource<NolPaySecretDataResponse, BaseRemoteRequest<NolPaySecretDataRequest>> {

    override suspend fun execute(input: BaseRemoteRequest<NolPaySecretDataRequest>) =
        primerHttpClient.postSuspend<NolPaySecretDataRequest, NolPaySecretDataResponse>(
            "${input.configuration.coreUrl}/nol-pay/sdk-secrets",
            input.data
        )
}
