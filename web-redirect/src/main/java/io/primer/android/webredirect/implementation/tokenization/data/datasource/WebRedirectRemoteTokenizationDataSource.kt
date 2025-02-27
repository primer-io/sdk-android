package io.primer.android.webredirect.implementation.tokenization.data.datasource

import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.webredirect.implementation.tokenization.data.model.WebRedirectPaymentInstrumentDataRequest

internal class WebRedirectRemoteTokenizationDataSource(
    primerHttpClient: PrimerHttpClient,
    apiVersion: () -> PrimerApiVersion,
) : BaseRemoteTokenizationDataSource<WebRedirectPaymentInstrumentDataRequest>(primerHttpClient, apiVersion)
