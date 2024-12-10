package io.primer.android.vault.implementation.vaultedMethods.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteUrlRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.vault.implementation.vaultedMethods.data.model.BasePaymentMethodVaultExchangeDataRequest
import io.primer.android.vault.implementation.vaultedMethods.data.model.card.CardVaultExchangeDataRequest
import io.primer.android.vault.implementation.vaultedMethods.data.model.empty.EmptyExchangeDataRequest
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal

internal abstract class RemoteVaultedPaymentMethodsExchangeDataSource<
    out T : BasePaymentMethodVaultExchangeDataRequest> :
    BaseSuspendDataSource<PaymentMethodTokenInternal, BaseRemoteUrlRequest<@UnsafeVariance T>>

internal class RemoteVaultedCardExchangeDataSource(
    private val primerHttpClient: PrimerHttpClient
) : RemoteVaultedPaymentMethodsExchangeDataSource<CardVaultExchangeDataRequest>() {

    override suspend fun execute(input: BaseRemoteUrlRequest<CardVaultExchangeDataRequest>) =
        primerHttpClient.suspendPost<CardVaultExchangeDataRequest, PaymentMethodTokenInternal>(
            url = input.url,
            request = input.data
        ).body
}

internal class RemoteEmptyExchangeDataSource(
    private val primerHttpClient: PrimerHttpClient
) : RemoteVaultedPaymentMethodsExchangeDataSource<EmptyExchangeDataRequest>() {

    override suspend fun execute(input: BaseRemoteUrlRequest<EmptyExchangeDataRequest>) =
        primerHttpClient.suspendPost<EmptyExchangeDataRequest, PaymentMethodTokenInternal>(
            url = input.url,
            request = input.data
        ).body
}
