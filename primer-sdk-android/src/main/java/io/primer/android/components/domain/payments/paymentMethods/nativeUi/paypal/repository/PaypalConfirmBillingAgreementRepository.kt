package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreement
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreementParams
import kotlinx.coroutines.flow.Flow

internal interface PaypalConfirmBillingAgreementRepository {

    fun confirmBillingAgreement(params: PaypalConfirmBillingAgreementParams):
        Flow<PaypalConfirmBillingAgreement>
}
