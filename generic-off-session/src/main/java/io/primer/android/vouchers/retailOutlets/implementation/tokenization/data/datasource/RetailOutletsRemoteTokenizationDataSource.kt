package io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.datasource

import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.model.RetailOutletsPaymentInstrumentDataRequest

internal class RetailOutletsRemoteTokenizationDataSource(primerHttpClient: PrimerHttpClient) :
    BaseRemoteTokenizationDataSource<RetailOutletsPaymentInstrumentDataRequest>(primerHttpClient)
