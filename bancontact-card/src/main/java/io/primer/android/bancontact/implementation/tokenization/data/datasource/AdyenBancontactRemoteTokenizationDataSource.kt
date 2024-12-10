package io.primer.android.bancontact.implementation.tokenization.data.datasource

import io.primer.android.bancontact.implementation.tokenization.data.model.AdyenBancontactPaymentInstrumentDataRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource

internal class AdyenBancontactRemoteTokenizationDataSource(primerHttpClient: PrimerHttpClient) :
    BaseRemoteTokenizationDataSource<AdyenBancontactPaymentInstrumentDataRequest>(primerHttpClient)
