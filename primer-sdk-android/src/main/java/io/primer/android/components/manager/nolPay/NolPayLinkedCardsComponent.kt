package io.primer.android.components.manager.nolPay

import io.primer.android.components.manager.core.composable.PrimerHeadlessComponent
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.PrimerNolPay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NolPayLinkedCardsComponent : PrimerHeadlessComponent {

    suspend fun getLinkedCards(
        mobileNumber: String,
        phoneCountryDiallingCode: String
    ) = runSuspendCatching {
        withContext(Dispatchers.IO) {
            PrimerNolPay.instance.getLinkedPaymentCards(mobileNumber, phoneCountryDiallingCode)
        }
    }
}
