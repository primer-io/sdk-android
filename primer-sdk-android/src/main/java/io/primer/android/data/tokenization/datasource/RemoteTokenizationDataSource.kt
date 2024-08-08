package io.primer.android.data.tokenization.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.data.tokenization.models.TokenizationRequest
import io.primer.android.data.tokenization.models.TokenizationRequestV2
import io.primer.android.di.ApiVersion
import io.primer.android.di.NetworkContainer.Companion.SDK_API_VERSION_HEADER
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

internal class RemoteTokenizationDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseFlowDataSource<PaymentMethodTokenInternal, BaseRemoteRequest<TokenizationRequestV2>> {

    fun executeV1(input: BaseRemoteRequest<TokenizationRequest>):
        Flow<PaymentMethodTokenInternal> {
        return primerHttpClient.post<TokenizationRequest, PaymentMethodTokenInternal>(
            "${input.configuration.pciUrl}/payment-instruments",
            input.data,
            mapOf(SDK_API_VERSION_HEADER to ApiVersion.PAYMENT_INSTRUMENTS_VERSION.version)
        ).mapLatest { responseData -> responseData.body }
    }

    override fun execute(input: BaseRemoteRequest<TokenizationRequestV2>):
        Flow<PaymentMethodTokenInternal> {
        return primerHttpClient.post<TokenizationRequestV2, PaymentMethodTokenInternal>(
            "${input.configuration.pciUrl}/payment-instruments",
            input.data,
            mapOf(SDK_API_VERSION_HEADER to ApiVersion.PAYMENT_INSTRUMENTS_VERSION.version)
        ).mapLatest { responseData -> responseData.body }
    }
}
