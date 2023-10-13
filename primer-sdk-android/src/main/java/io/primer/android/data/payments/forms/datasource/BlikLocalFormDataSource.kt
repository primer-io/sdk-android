package io.primer.android.data.payments.forms.datasource

import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.R
import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import io.primer.android.data.payments.forms.models.FormInputDataResponse
import io.primer.android.data.payments.forms.models.FormType
import kotlinx.coroutines.flow.flowOf

internal class BlikLocalFormDataSource(private val theme: PrimerTheme) :
    BaseFlowCacheDataSource<FormDataResponse, String> {

    override fun get() = flowOf(
        FormDataResponse(
            null,
            if (theme.isDarkMode == true) {
                R.drawable.ic_logo_blik
            } else { R.drawable.ic_logo_blik_light },
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

        const val FORM_ID = "blikCode"
        const val FORM_MAX_INPUT = 6
        const val FORM_VALIDATION = "^(\\d){6}$"
    }
}
