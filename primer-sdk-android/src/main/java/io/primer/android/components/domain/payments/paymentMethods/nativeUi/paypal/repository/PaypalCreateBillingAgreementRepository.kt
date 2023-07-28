package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalBillingAgreement
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateBillingAgreementParams
import kotlinx.coroutines.flow.Flow

internal interface PaypalCreateBillingAgreementRepository {

    fun createBillingAgreement(params: PaypalCreateBillingAgreementParams):
        Flow<PaypalBillingAgreement>
}
