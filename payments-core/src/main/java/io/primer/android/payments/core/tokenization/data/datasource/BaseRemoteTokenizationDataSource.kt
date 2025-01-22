package io.primer.android.payments.core.tokenization.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import io.primer.android.payments.core.tokenization.data.model.BasePaymentInstrumentDataRequest
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2

open class BaseRemoteTokenizationDataSource<T : BasePaymentInstrumentDataRequest>(
    private val primerHttpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : BaseSuspendDataSource<PaymentMethodTokenInternal, BaseRemoteHostRequest<TokenizationRequestV2<T>>> {
    override suspend fun execute(input: BaseRemoteHostRequest<TokenizationRequestV2<T>>): PaymentMethodTokenInternal {
        return primerHttpClient.withTimeout(PRIMER_15S_TIMEOUT)
            .suspendPost<TokenizationRequestV2<T>, PaymentMethodTokenInternal>(
                url = "${input.host}/payment-instruments",
                request = input.data,
                headers = apiVersion().toHeaderMap(),
            ).body
    }
}
