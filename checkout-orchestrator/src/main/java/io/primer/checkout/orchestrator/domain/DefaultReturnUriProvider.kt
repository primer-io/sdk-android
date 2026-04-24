package io.primer.checkout.orchestrator.domain

import android.net.Uri
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.paymentmethods.common.utils.Constants

internal class DefaultReturnUriProvider(
    private val applicationIdProvider: BaseDataProvider<String>,
) : ReturnUriProvider {
    override suspend fun provide(): String {
        return Uri.Builder()
            .scheme(Constants.PRIMER_REDIRECT_SCHEMA)
            .authority("${Constants.PRIMER_REDIRECT_PREFIX}${applicationIdProvider.provide()}")
            .appendPath(REDIRECT_PATH)
            .build()
            .toString()
    }

    private companion object {
        const val REDIRECT_PATH = "async"
    }
}
