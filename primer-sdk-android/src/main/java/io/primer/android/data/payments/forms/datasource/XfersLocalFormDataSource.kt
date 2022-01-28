package io.primer.android.data.payments.forms.datasource

import io.primer.android.R
import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import io.primer.android.data.token.model.ClientToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class XfersLocalFormDataSource(private val clientToken: ClientToken) :
    BaseFlowCacheDataSource<FormDataResponse, String> {

    override fun get(): Flow<FormDataResponse> = flowOf(
        FormDataResponse(
            R.string.payment_method_xfers_title,
            R.drawable.ic_logo_xfers_colored,
            ButtonType.CONFIRM,
            R.string.payment_method_xfers_description,
            qrCode = clientToken.qrCode
        )
    )
}
