package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource.RemotePaypalCreateBillingAgreementDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateBillingAgreementDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.toBillingAgreement
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalBillingAgreement
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateBillingAgreementParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalCreateBillingAgreementRepository
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.extensions.doOnError
import io.primer.android.http.exception.HttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PaypalCreateBillingAgreementDataRepository(
    private val createBillingAgreementDataSource: RemotePaypalCreateBillingAgreementDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource,
) : PaypalCreateBillingAgreementRepository {
    override fun createBillingAgreement(params: PaypalCreateBillingAgreementParams):
        Flow<PaypalBillingAgreement> {
        return createBillingAgreementDataSource.execute(
            BaseRemoteRequest(
                localConfigurationDataSource.getConfiguration(),
                PaypalCreateBillingAgreementDataRequest(
                    params.paymentMethodConfigId,
                    params.successUrl,
                    params.cancelUrl,
                )
            )
        ).map {
            it.toBillingAgreement(
                params.paymentMethodConfigId,
                params.successUrl,
                params.cancelUrl
            )
        }
            .doOnError {
                when {
                    it is HttpException && it.isClientError() ->
                        throw SessionCreateException(
                            PaymentMethodType.PAYPAL,
                            it.error.diagnosticsId
                        )
                    else -> throw it
                }
            }
    }
}
