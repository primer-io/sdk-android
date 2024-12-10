package io.primer.android.data.payments.forms.datasource

import io.primer.android.R
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.core.data.datasource.BaseFlowCacheDataSource
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import io.primer.android.data.payments.forms.models.FormInputDataResponse
import io.primer.android.data.payments.forms.models.FormType
import kotlinx.coroutines.flow.flowOf

internal class BlikLocalFormDataSource(private val theme: PrimerTheme) :
    BaseFlowCacheDataSource<FormDataResponse, String> {

    override fun get() = flowOf(
        FormDataResponse(
            title = null,
            logo = if (theme.isDarkMode == true) {
                R.drawable.ic_logo_blik
            } else {
                R.drawable.ic_logo_blik_light
            },
            buttonType = ButtonType.CONFIRM,
            description = R.string.input_description_otp,
            inputs = listOf(
                FormInputDataResponse(
                    type = FormType.NUMBER,
                    id = FORM_ID,
                    hint = R.string.input_hint_form_blik_otp,
                    level = null,
                    mask = null,
                    inputCharacters = null
                )
            )
        )
    )

    private companion object {

        const val FORM_ID = "blikCode"
    }
}
