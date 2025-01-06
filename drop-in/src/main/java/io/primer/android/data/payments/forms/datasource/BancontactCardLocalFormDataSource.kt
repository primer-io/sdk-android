package io.primer.android.data.payments.forms.datasource

import io.primer.android.R
import io.primer.android.core.data.datasource.BaseFlowCacheDataSource
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import io.primer.android.ui.settings.PrimerTheme
import kotlinx.coroutines.flow.flowOf

internal class BancontactCardLocalFormDataSource(
    private val theme: PrimerTheme,
) : BaseFlowCacheDataSource<FormDataResponse, String> {
    override fun get() =
        flowOf(
            FormDataResponse(
                null,
                if (theme.isDarkMode == true) {
                    R.drawable.ic_logo_bancontact_dark
                } else {
                    R.drawable.ic_logo_bancontact
                },
                ButtonType.PAY,
                null,
            ),
        )
}
