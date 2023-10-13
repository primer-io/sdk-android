package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource.RemotePaypalConfirmBillingAgreementDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreementDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.toPaypalConfirmBillingAgreement
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreement
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreementParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalConfirmBillingAgreementRepository
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.extensions.doOnError
import io.primer.android.http.exception.HttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PaypalConfirmBillingAgreementDataRepository(
    private val confirmBillingAgreementDataSource: RemotePaypalConfirmBillingAgreementDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : PaypalConfirmBillingAgreementRepository {

    override fun confirmBillingAgreement(params: PaypalConfirmBillingAgreementParams):
        Flow<PaypalConfirmBillingAgreement> {
        return confirmBillingAgreementDataSource.execute(
            BaseRemoteRequest(
                localConfigurationDataSource.getConfiguration(),
                PaypalConfirmBillingAgreementDataRequest(
                    params.paymentMethodConfigId,
                    params.tokenId
                )
            )
        ).map { it.toPaypalConfirmBillingAgreement() }
            .doOnError {
                when {
                    it is HttpException && it.isClientError() ->
                        throw SessionCreateException(
                            PaymentMethodType.PAYPAL,
                            it.error.diagnosticsId,
                            it.error.description
                        )
                    else -> throw it
                }
            }
    }
}
