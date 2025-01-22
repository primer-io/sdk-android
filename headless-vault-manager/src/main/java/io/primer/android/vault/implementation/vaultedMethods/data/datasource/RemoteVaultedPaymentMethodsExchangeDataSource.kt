package io.primer.android.vault.implementation.vaultedMethods.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.model.BaseRemoteUrlRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import io.primer.android.core.di.DISdkComponent
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.vault.implementation.vaultedMethods.data.model.BasePaymentMethodVaultExchangeDataRequest
import io.primer.android.vault.implementation.vaultedMethods.data.model.card.CardVaultExchangeDataRequest
import io.primer.android.vault.implementation.vaultedMethods.data.model.empty.EmptyExchangeDataRequest

internal abstract class RemoteVaultedPaymentMethodsExchangeDataSource<
    out T : BasePaymentMethodVaultExchangeDataRequest,
    > :
    BaseSuspendDataSource<PaymentMethodTokenInternal, BaseRemoteUrlRequest<@UnsafeVariance T>>

internal class RemoteVaultedCardExchangeDataSource(
    private val primerHttpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : RemoteVaultedPaymentMethodsExchangeDataSource<CardVaultExchangeDataRequest>() {
    override suspend fun execute(input: BaseRemoteUrlRequest<CardVaultExchangeDataRequest>) =
        primerHttpClient.withTimeout(PRIMER_15S_TIMEOUT)
            .suspendPost<CardVaultExchangeDataRequest, PaymentMethodTokenInternal>(
                url = input.url,
                request = input.data,
                headers = apiVersion().toHeaderMap(),
            ).body
}

internal class RemoteEmptyExchangeDataSource(
    private val primerHttpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : DISdkComponent, RemoteVaultedPaymentMethodsExchangeDataSource<EmptyExchangeDataRequest>() {
    override suspend fun execute(input: BaseRemoteUrlRequest<EmptyExchangeDataRequest>) =
        primerHttpClient.withTimeout(PRIMER_15S_TIMEOUT)
            .suspendPost<EmptyExchangeDataRequest, PaymentMethodTokenInternal>(
                url = input.url,
                request = input.data,
                headers = apiVersion().toHeaderMap(),
            ).body
}
