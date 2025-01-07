package io.primer.android.data.payments.forms.datasource

import io.primer.android.R
import io.primer.android.core.data.datasource.BaseFlowCacheDataSource
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class XfersLocalFormDataSource :
    BaseFlowCacheDataSource<FormDataResponse, String> {
    override fun get(): Flow<FormDataResponse> =
        flowOf(
            FormDataResponse(
                title = R.string.payment_method_xfers_title,
                logo = R.drawable.ic_logo_xfers_colored,
                buttonType = ButtonType.CONFIRM,
                description = R.string.payment_method_xfers_description,
            ),
        )
}
