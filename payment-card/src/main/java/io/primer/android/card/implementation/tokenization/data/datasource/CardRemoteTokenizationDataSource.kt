package io.primer.android.card.implementation.tokenization.data.datasource

import io.primer.android.card.implementation.tokenization.data.model.CardPaymentInstrumentDataRequest
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource

internal class CardRemoteTokenizationDataSource(
    primerHttpClient: PrimerHttpClient,
    apiVersion: () -> PrimerApiVersion,
) : BaseRemoteTokenizationDataSource<CardPaymentInstrumentDataRequest>(primerHttpClient, apiVersion)
