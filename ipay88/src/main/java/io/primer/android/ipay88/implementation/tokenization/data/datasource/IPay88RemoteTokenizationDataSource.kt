package io.primer.android.ipay88.implementation.tokenization.data.datasource

import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.ipay88.implementation.tokenization.data.model.IPay88PaymentInstrumentDataRequest

internal class IPay88RemoteTokenizationDataSource(primerHttpClient: PrimerHttpClient) :
    BaseRemoteTokenizationDataSource<IPay88PaymentInstrumentDataRequest>(primerHttpClient)
