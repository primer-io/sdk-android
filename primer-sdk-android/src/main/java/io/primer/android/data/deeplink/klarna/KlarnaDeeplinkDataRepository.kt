package io.primer.android.data.deeplink.klarna

import android.net.Uri
import io.primer.android.domain.deeplink.klarna.repository.KlarnaDeeplinkRepository
import io.primer.android.infrastructure.metadata.datasource.MetaDataSource

internal class KlarnaDeeplinkDataRepository(
    private val metaDataSource: MetaDataSource
) : KlarnaDeeplinkRepository {

    override fun getDeeplinkUrl() =
        Uri.Builder().scheme(PRIMER_REDIRECT_SCHEMA)
            .authority("$PRIMER_REDIRECT_PREFIX${metaDataSource.getApplicationId()}")
            .build()
            .toString()

    private companion object {

        const val PRIMER_REDIRECT_SCHEMA = "primer_klarna"
        const val PRIMER_REDIRECT_PREFIX = "requestor."
    }
}
