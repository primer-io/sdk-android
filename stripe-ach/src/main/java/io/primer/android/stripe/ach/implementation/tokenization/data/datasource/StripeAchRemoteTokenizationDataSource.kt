package io.primer.android.stripe.ach.implementation.tokenization.data.datasource

import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.stripe.ach.implementation.tokenization.data.model.StripeAchPaymentInstrumentDataRequest

internal class StripeAchRemoteTokenizationDataSource(primerHttpClient: PrimerHttpClient) :
    BaseRemoteTokenizationDataSource<StripeAchPaymentInstrumentDataRequest>(primerHttpClient)
