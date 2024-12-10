package io.primer.android.paypal.implementation.tokenization.data.datasource

import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalPaymentInstrumentDataRequest

internal class PaypalRemoteTokenizationDataSource(primerHttpClient: PrimerHttpClient) :
    BaseRemoteTokenizationDataSource<PaypalPaymentInstrumentDataRequest>(primerHttpClient)
