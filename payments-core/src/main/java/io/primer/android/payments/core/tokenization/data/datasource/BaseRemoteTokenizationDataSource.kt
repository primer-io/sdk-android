package io.primer.android.payments.core.tokenization.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.Constants.SDK_API_VERSION_HEADER
import io.primer.android.payments.core.tokenization.data.model.BasePaymentInstrumentDataRequest
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2

open class BaseRemoteTokenizationDataSource<T : BasePaymentInstrumentDataRequest>(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<PaymentMethodTokenInternal, BaseRemoteHostRequest<TokenizationRequestV2<T>>> {
    override suspend fun execute(input: BaseRemoteHostRequest<TokenizationRequestV2<T>>): PaymentMethodTokenInternal {
        return primerHttpClient.suspendPost<TokenizationRequestV2<T>, PaymentMethodTokenInternal>(
            url = "${input.host}/payment-instruments",
            request = input.data,
            headers = mapOf(SDK_API_VERSION_HEADER to PAYMENT_INSTRUMENTS_VERSION),
        ).body
    }

    private companion object {
        const val PAYMENT_INSTRUMENTS_VERSION = "2.2"
    }
}
