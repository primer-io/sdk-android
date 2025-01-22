package io.primer.android.banks.implementation.tokenization.data.datasource

import io.primer.android.banks.implementation.tokenization.data.model.BankIssuerPaymentInstrumentDataRequest
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource

internal class BankIssuerRemoteTokenizationDataSource(
    primerHttpClient: PrimerHttpClient,
    apiVersion: () -> PrimerApiVersion,
) : BaseRemoteTokenizationDataSource<BankIssuerPaymentInstrumentDataRequest>(primerHttpClient, apiVersion)
