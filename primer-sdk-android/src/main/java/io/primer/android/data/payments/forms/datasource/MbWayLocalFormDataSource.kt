package io.primer.android.data.payments.forms.datasource

import io.primer.android.R
import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import io.primer.android.data.payments.forms.models.FormInputDataResponse
import io.primer.android.data.payments.forms.models.FormType
import kotlinx.coroutines.flow.flowOf

internal class MbWayLocalFormDataSource :
    BaseFlowCacheDataSource<FormDataResponse, String> {

    override fun get() = flowOf(
        FormDataResponse(
            R.string.input_title_phone_number,
            R.drawable.ic_logo_mbway_light,
            ButtonType.CONFIRM,
            null,
            listOf(
                FormInputDataResponse(
                    FormType.PHONE,
                    FORM_ID,
                    R.string.input_hint_form_phone_number,
                    null,
                    null,
                    null,
                    null,
                    FORM_VALIDATION
                )
            )
        )
    )

    private companion object {

        const val FORM_ID = "phoneNumber"
        const val FORM_VALIDATION = ".*\\S.*"
    }
}
