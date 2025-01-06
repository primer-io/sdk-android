package io.primer.android.paypal.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.extensions.onError
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paypal.implementation.tokenization.data.datasource.RemotePaypalConfirmBillingAgreementDataSource
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalConfirmBillingAgreementDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.toPaypalConfirmBillingAgreement
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalConfirmBillingAgreement
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalConfirmBillingAgreementParams
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalConfirmBillingAgreementRepository

internal class PaypalConfirmBillingAgreementDataRepository(
    private val confirmBillingAgreementDataSource: RemotePaypalConfirmBillingAgreementDataSource,
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
) : PaypalConfirmBillingAgreementRepository {
    override suspend fun confirmBillingAgreement(
        params: PaypalConfirmBillingAgreementParams,
    ): Result<PaypalConfirmBillingAgreement> {
        return runSuspendCatching {
            confirmBillingAgreementDataSource.execute(
                BaseRemoteHostRequest(
                    configurationDataSource.get().coreUrl,
                    PaypalConfirmBillingAgreementDataRequest(
                        params.paymentMethodConfigId,
                        params.tokenId,
                    ),
                ),
            ).toPaypalConfirmBillingAgreement()
        }.onError {
            when {
                it is HttpException && it.isClientError() ->
                    throw SessionCreateException(
                        PaymentMethodType.PAYPAL.name,
                        it.error.diagnosticsId,
                        it.error.description,
                    )

                else -> throw it
            }
        }
    }
}
