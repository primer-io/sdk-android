package io.primer.android.domain.payments.apaya.validation

import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import kotlinx.coroutines.flow.flow
import java.util.Currency

internal class ApayaSessionParamsValidator {

    fun validate(sessionParams: ApayaSessionParams) =
        flow {
            require(sessionParams.merchantAccountId.isNotBlank()) { INVALID_SESSION_PARAMS }
            require(sessionParams.locale.language.isNotBlank()) { INVALID_SESSION_PARAMS }
            require(Currency.getInstance(sessionParams.currencyCode) != null)
            emit(Unit)
        }

    internal companion object {

        const val INVALID_SESSION_PARAMS = "The Apaya flow launched, but the SDK client token or " +
            "payment method config was missing."
    }
}
