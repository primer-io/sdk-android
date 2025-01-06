package io.primer.android.threeds.data.repository

import android.net.Uri
import android.webkit.URLUtil
import androidx.core.util.PatternsCompat
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.core.extensions.buildWithQueryParams
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.threeds.domain.repository.ThreeDsAppUrlRepository

internal class ThreeDsAppUrlDataRepository(private val settings: PrimerSettings) :
    ThreeDsAppUrlRepository {
    override fun getAppUrl(transaction: Transaction): String? {
        return settings.paymentMethodOptions.threeDsOptions.threeDsAppRequestorUrl?.takeIf { url ->
            URLUtil.isHttpsUrl(url) && PatternsCompat.WEB_URL.matcher(url).matches()
        }?.let { url ->
            Uri.parse(url)
                .buildWithQueryParams(
                    mapOf(
                        PRIMER_THREE_DS_TRANSACTION_ID_QUERY to
                            transaction.authenticationRequestParameters.sdkTransactionID,
                    ),
                )
        }
    }

    private companion object {
        const val PRIMER_THREE_DS_TRANSACTION_ID_QUERY = "transID"
    }
}
