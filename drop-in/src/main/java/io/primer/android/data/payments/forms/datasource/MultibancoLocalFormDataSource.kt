package io.primer.android.data.payments.forms.datasource

import io.primer.android.R
import io.primer.android.core.data.datasource.BaseFlowCacheDataSource
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import io.primer.android.ui.settings.PrimerTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class MultibancoLocalFormDataSource(private val theme: PrimerTheme) :
    BaseFlowCacheDataSource<FormDataResponse, String> {
    override fun get(): Flow<FormDataResponse> =
        flowOf(
            FormDataResponse(
                R.string.completeYourPayment,
                if (theme.isDarkMode == true) {
                    R.drawable.ic_logo_multibanco_dark
                } else {
                    R.drawable.ic_logo_multibanco_light
                },
                ButtonType.CONFIRM,
                description = R.string.multibancoCompleteDescription,
            ),
        )
}
