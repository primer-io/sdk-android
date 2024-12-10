package io.primer.android.ipay88.implementation.deeplink.data.repository

import android.net.Uri
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.ipay88.implementation.deeplink.domain.repository.IPay88DeeplinkRepository
import io.primer.android.paymentmethods.common.utils.Constants

internal class IPay88DeeplinkDataRepository(
    private val applicationIdProvider: BaseDataProvider<String>
) : IPay88DeeplinkRepository {

    override fun getDeeplinkUrl(): String {
        return Uri.Builder()
            .scheme(Constants.PRIMER_REDIRECT_SCHEMA)
            .authority("${Constants.PRIMER_REDIRECT_PREFIX}${applicationIdProvider.provide()}")
            .appendPath(PRIMER_REDIRECT_PATH_PREFIX)
            .build()
            .toString()
    }

    private companion object {
        const val PRIMER_REDIRECT_PATH_PREFIX = "ipay88"
    }
}
