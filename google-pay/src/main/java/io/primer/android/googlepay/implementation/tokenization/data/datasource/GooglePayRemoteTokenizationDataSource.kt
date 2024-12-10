package io.primer.android.googlepay.implementation.tokenization.data.datasource

import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.googlepay.implementation.tokenization.data.model.GooglePayPaymentInstrumentDataRequest
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource

internal class GooglePayRemoteTokenizationDataSource(primerHttpClient: PrimerHttpClient) :
    BaseRemoteTokenizationDataSource<GooglePayPaymentInstrumentDataRequest>(primerHttpClient)
