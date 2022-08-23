package io.primer.android.data.deeplink.async

import android.net.Uri
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.infrastructure.metadata.datasource.MetaDataSource

internal class AsyncPaymentMethodDeeplinkDataRepository(
    private val metaDataSource: MetaDataSource
) : AsyncPaymentMethodDeeplinkRepository {

    override fun getDeeplinkUrl() =
        Uri.Builder().scheme(PRIMER_REDIRECT_SCHEMA)
            .authority("$PRIMER_REDIRECT_PREFIX${metaDataSource.getApplicationId()}")
            .build()
            .toString()

    private companion object {

        const val PRIMER_REDIRECT_SCHEMA = "primersdk"
        const val PRIMER_REDIRECT_PREFIX = "requestor."
    }
}
