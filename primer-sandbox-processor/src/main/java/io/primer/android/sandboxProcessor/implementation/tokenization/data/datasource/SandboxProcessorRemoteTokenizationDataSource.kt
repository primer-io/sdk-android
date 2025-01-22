package io.primer.android.sandboxProcessor.implementation.tokenization.data.datasource

import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.sandboxProcessor.implementation.tokenization.data.model.SandboxProcessorPaymentInstrumentDataRequest

internal class SandboxProcessorRemoteTokenizationDataSource(
    primerHttpClient: PrimerHttpClient,
    apiVersion: () -> PrimerApiVersion,
) : BaseRemoteTokenizationDataSource<SandboxProcessorPaymentInstrumentDataRequest>(primerHttpClient, apiVersion)
