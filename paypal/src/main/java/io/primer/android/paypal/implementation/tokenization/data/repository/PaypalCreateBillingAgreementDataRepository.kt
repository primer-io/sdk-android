package io.primer.android.paypal.implementation.tokenization.data.repository

import io.primer.android.paypal.implementation.tokenization.data.datasource.RemotePaypalCreateBillingAgreementDataSource
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalCreateBillingAgreementDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.toBillingAgreement
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.extensions.onError
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalBillingAgreement
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateBillingAgreementParams
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalCreateBillingAgreementRepository

internal class PaypalCreateBillingAgreementDataRepository(
    private val createBillingAgreementDataSource: RemotePaypalCreateBillingAgreementDataSource,
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
) : PaypalCreateBillingAgreementRepository {
    override suspend fun createBillingAgreement(params: PaypalCreateBillingAgreementParams):
        Result<PaypalBillingAgreement> = runSuspendCatching {
        createBillingAgreementDataSource.execute(
            BaseRemoteHostRequest(
                configurationDataSource.get().coreUrl,
                PaypalCreateBillingAgreementDataRequest(
                    params.paymentMethodConfigId,
                    params.successUrl,
                    params.cancelUrl
                )
            )
        ).toBillingAgreement(
            params.paymentMethodConfigId,
            params.successUrl,
            params.cancelUrl
        )
    }.onError {
        when {
            it is HttpException && it.isClientError() ->
                throw SessionCreateException(
                    PaymentMethodType.PAYPAL.name,
                    it.error.diagnosticsId,
                    it.error.description
                )

            else -> throw it
        }
    }
}
