package io.primer.android.domain.payments.apaya.validation

import io.primer.android.domain.payments.apaya.models.ApayaWebResultParams
import kotlinx.coroutines.flow.flow

internal class ApayaWebResultValidator {

    fun validate(webResultParams: ApayaWebResultParams) =
        flow {
            require(webResultParams.mcc.isNotBlank()) { INVALID_WEB_VIEW_RESULT }
            require(webResultParams.mnc.isNotBlank()) { INVALID_WEB_VIEW_RESULT }
            require(webResultParams.mxNumber.isNotBlank()) { INVALID_WEB_VIEW_RESULT }
            require(webResultParams.hashedIdentifier.isNotBlank()) { INVALID_WEB_VIEW_RESULT }
            require(webResultParams.success == RESULT_SUCCESS) { INVALID_WEB_VIEW_RESULT }

            emit(Unit)
        }

    internal companion object {

        const val RESULT_SUCCESS = "1"
        const val INVALID_WEB_VIEW_RESULT =
            "The Apaya web view redirection query parameter parsing failed."
    }
}
