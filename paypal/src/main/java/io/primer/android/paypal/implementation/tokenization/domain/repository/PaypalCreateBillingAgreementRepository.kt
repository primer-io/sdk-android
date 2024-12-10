package io.primer.android.paypal.implementation.tokenization.domain.repository

import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalBillingAgreement
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateBillingAgreementParams

internal interface PaypalCreateBillingAgreementRepository {

    suspend fun createBillingAgreement(params: PaypalCreateBillingAgreementParams):
        Result<PaypalBillingAgreement>
}
