package io.primer.android.data.deeplink.ipay88

import android.net.Uri
import io.primer.android.domain.deeplink.ipay88.repository.IPay88DeeplinkRepository
import io.primer.android.infrastructure.metadata.datasource.MetaDataSource

internal class IPay88DeeplinkDataRepository(private val metaDataSource: MetaDataSource) :
    IPay88DeeplinkRepository {
    override fun getDeeplinkUrl() =
        Uri.Builder().scheme(PRIMER_REDIRECT_SCHEMA)
            .authority("$PRIMER_REDIRECT_PREFIX${metaDataSource.getApplicationId()}")
            .build()
            .toString()

    private companion object {

        const val PRIMER_REDIRECT_SCHEMA = "primer_ipay88"
        const val PRIMER_REDIRECT_PREFIX = "requestor."
    }
}
