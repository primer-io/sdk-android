package io.primer.android.nolpay.implementation.errors.domain.model

import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import java.util.UUID

internal class NolPayError(override val errorCode: String, errorMessage: String?) : PrimerError() {
    override val errorId = "nol-pay-sdk-error"
    override val description = "Nol SDK encountered an error $errorCode. $errorMessage"
    override val diagnosticsId = UUID.randomUUID().toString()
    override val exposedError = this
    override val recoverySuggestion: String? = null
    override val context: BaseContextParams =
        ErrorContextParams(errorId, PaymentMethodType.NOL_PAY.name)
}
