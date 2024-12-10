package io.primer.android.vouchers.multibanco.implementation.tokenization.data.datasource

import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.vouchers.multibanco.implementation.tokenization.data.model.MultibancoPaymentInstrumentDataRequest

internal class MultibancoRemoteTokenizationDataSource(primerHttpClient: PrimerHttpClient) :
    BaseRemoteTokenizationDataSource<MultibancoPaymentInstrumentDataRequest>(primerHttpClient)
