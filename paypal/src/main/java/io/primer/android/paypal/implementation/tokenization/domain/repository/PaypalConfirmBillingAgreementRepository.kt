package io.primer.android.paypal.implementation.tokenization.domain.repository

import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalConfirmBillingAgreement
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalConfirmBillingAgreementParams

internal interface PaypalConfirmBillingAgreementRepository {

    suspend fun confirmBillingAgreement(params: PaypalConfirmBillingAgreementParams):
        Result<PaypalConfirmBillingAgreement>
}
