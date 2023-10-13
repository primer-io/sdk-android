package io.primer.android.data.payments.forms.datasource

import io.primer.android.R
import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import io.primer.android.data.token.model.ClientToken
import io.primer.android.ui.settings.PrimerTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FastBankTransferLocalFormDataSource(
    private val theme: PrimerTheme,
    private val clientToken: ClientToken
) : BaseFlowCacheDataSource<FormDataResponse, String> {

    override fun get(): Flow<FormDataResponse> = flowOf(
        FormDataResponse(
            R.string.completeYourPayment,
            if (theme.isDarkMode == true) {
                R.drawable.ic_logo_fast_dark
            } else { R.drawable.ic_logo_fast_light },
            ButtonType.CONFIRM,
            description = R.string.pleaseTransferFunds,
            qrCode = clientToken.qrCode,
            accountNumber = clientToken.accountNumber,
            expiration = clientToken.expiration
        )
    )
}
