package io.primer.android.phoneNumber.implementation.tokenization.data.datasource

import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.phoneNumber.implementation.tokenization.data.model.PhoneNumberPaymentInstrumentDataRequest

internal class PhoneNumberRemoteTokenizationDataSource(
    primerHttpClient: PrimerHttpClient,
    apiVersion: () -> PrimerApiVersion,
) : BaseRemoteTokenizationDataSource<PhoneNumberPaymentInstrumentDataRequest>(primerHttpClient, apiVersion)
