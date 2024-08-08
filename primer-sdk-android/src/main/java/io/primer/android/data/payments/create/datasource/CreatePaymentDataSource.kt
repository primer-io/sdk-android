package io.primer.android.data.payments.create.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.payments.create.models.CreatePaymentDataRequest
import io.primer.android.data.payments.create.models.PaymentDataResponse
import io.primer.android.di.ApiVersion
import io.primer.android.di.NetworkContainer.Companion.SDK_API_VERSION_HEADER
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal const val HEADER_ACCEPT = "accept"

internal class CreatePaymentDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseFlowDataSource<PaymentDataResponse, BaseRemoteRequest<CreatePaymentDataRequest>> {

    override fun execute(input: BaseRemoteRequest<CreatePaymentDataRequest>):
        Flow<PaymentDataResponse> {
        return primerHttpClient.post<CreatePaymentDataRequest, PaymentDataResponse>(
            "${input.configuration.pciUrl}/payments",
            input.data,
            mapOf(
                SDK_API_VERSION_HEADER to ApiVersion.PAYMENTS_VERSION.version,
                HEADER_ACCEPT to "*/*"
            )
        ).map { responseData -> responseData.body }
    }
}
