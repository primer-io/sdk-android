package io.primer.android.nolpay.implementation.paymentCard.tokenization.data.datasource

import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.nolpay.implementation.paymentCard.tokenization.data.model.NolPayPaymentInstrumentDataRequest
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource

internal class NolPayRemoteTokenizationDataSource(primerHttpClient: PrimerHttpClient) :
    BaseRemoteTokenizationDataSource<NolPayPaymentInstrumentDataRequest>(primerHttpClient)
