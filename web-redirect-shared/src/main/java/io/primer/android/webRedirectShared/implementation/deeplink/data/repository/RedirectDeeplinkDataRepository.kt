package io.primer.android.webRedirectShared.implementation.deeplink.data.repository

import android.net.Uri
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.paymentmethods.common.utils.Constants
import io.primer.android.webRedirectShared.implementation.deeplink.domain.repository.RedirectDeeplinkRepository

class RedirectDeeplinkDataRepository(
    private val applicationIdProvider: BaseDataProvider<String>
) : RedirectDeeplinkRepository {

    override fun getDeeplinkUrl(): String {
        return Uri.Builder()
            .scheme(Constants.PRIMER_REDIRECT_SCHEMA)
            .authority("${Constants.PRIMER_REDIRECT_PREFIX}${applicationIdProvider.provide()}")
            .appendPath(PRIMER_REDIRECT_PATH_PREFIX)
            .build()
            .toString()
    }

    private companion object {
        const val PRIMER_REDIRECT_PATH_PREFIX = "async"
    }
}
