package io.primer.android.data.payments.forms.datasource

import io.primer.android.R
import io.primer.android.core.data.datasource.BaseFlowCacheDataSource
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import io.primer.android.ui.settings.PrimerTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class PromptPayLocalFormDataSource(private val theme: PrimerTheme) :
    BaseFlowCacheDataSource<FormDataResponse, String> {
    override fun get(): Flow<FormDataResponse> =
        flowOf(
            FormDataResponse(
                title = R.string.scanToPay,
                logo =
                    if (theme.isDarkMode == true) {
                        R.drawable.ic_logo_promptpay_dark
                    } else {
                        R.drawable.ic_logo_promptpay_light
                    },
                buttonType = ButtonType.CONFIRM,
                description = R.string.uploadScreenshot,
            ),
        )
}
