package io.primer.android.data.payments.forms.datasource

import io.primer.android.R
import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import io.primer.android.data.payments.forms.models.FormInputDataResponse
import io.primer.android.data.payments.forms.models.FormType
import kotlinx.coroutines.flow.flowOf

internal class BlikLocalFormDataSource : BaseFlowCacheDataSource<FormDataResponse, String> {

    override fun get() = flowOf(
        FormDataResponse(
            null,
            R.drawable.ic_logo_blik_light,
            ButtonType.CONFIRM,
            R.string.input_description_otp,
            listOf(
                FormInputDataResponse(
                    FormType.NUMBER,
                    FORM_ID,
                    R.string.input_hint_form_blik_otp,
                    null,
                    null,
                    null,
                    FORM_MAX_INPUT,
                    FORM_VALIDATION
                )
            )
        )
    )

    private companion object {

        const val FORM_ID = "otpCode"
        const val FORM_MAX_INPUT = 6
        const val FORM_VALIDATION = "^(\\d){6}$"
    }
}
