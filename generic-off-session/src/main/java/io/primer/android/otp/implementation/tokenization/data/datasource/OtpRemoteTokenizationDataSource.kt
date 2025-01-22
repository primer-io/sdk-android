package io.primer.android.otp.implementation.tokenization.data.datasource

import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.otp.implementation.tokenization.data.model.OtpPaymentInstrumentDataRequest
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource

internal class OtpRemoteTokenizationDataSource(
    primerHttpClient: PrimerHttpClient,
    apiVersion: () -> PrimerApiVersion,
) : BaseRemoteTokenizationDataSource<OtpPaymentInstrumentDataRequest>(primerHttpClient, apiVersion)
