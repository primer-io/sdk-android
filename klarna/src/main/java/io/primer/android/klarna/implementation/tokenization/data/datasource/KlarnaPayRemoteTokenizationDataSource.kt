package io.primer.android.klarna.implementation.tokenization.data.datasource

import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.klarna.implementation.tokenization.data.model.KlarnaPaymentInstrumentDataRequest

internal class KlarnaPayRemoteTokenizationDataSource(primerHttpClient: PrimerHttpClient) :
    BaseRemoteTokenizationDataSource<KlarnaPaymentInstrumentDataRequest>(primerHttpClient)
