package io.primer.android.data.payments.methods.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteUrlRequest
import io.primer.android.data.payments.methods.models.BasePaymentMethodVaultExchangeDataRequest
import io.primer.android.data.payments.methods.models.card.CardVaultExchangeDataRequest
import io.primer.android.data.payments.methods.models.empty.EmptyExchangeDataRequest
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.map

internal abstract class RemoteVaultedPaymentMethodsExchangeDataSource<
    out T : BasePaymentMethodVaultExchangeDataRequest> :
    BaseFlowDataSource<PaymentMethodTokenInternal, BaseRemoteUrlRequest<@UnsafeVariance T>>

internal class RemoteVaultedCardExchangeDataSource(
    private val primerHttpClient: PrimerHttpClient
) : RemoteVaultedPaymentMethodsExchangeDataSource<CardVaultExchangeDataRequest>() {

    override fun execute(input: BaseRemoteUrlRequest<CardVaultExchangeDataRequest>) =
        primerHttpClient.post<CardVaultExchangeDataRequest, PaymentMethodTokenInternal>(
            input.url,
            input.data
        ).map { responseData -> responseData.body }
}

internal class RemoteEmptyExchangeDataSource(
    private val primerHttpClient: PrimerHttpClient
) : RemoteVaultedPaymentMethodsExchangeDataSource<EmptyExchangeDataRequest>() {

    override fun execute(input: BaseRemoteUrlRequest<EmptyExchangeDataRequest>) =
        primerHttpClient.post<EmptyExchangeDataRequest, PaymentMethodTokenInternal>(
            input.url,
            input.data
        ).map { responseData -> responseData.body }
}
