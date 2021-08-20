package io.primer.android.threeds.data.repository

import android.net.Uri
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.infrastructure.metadata.datasource.MetaDataSource
import io.primer.android.threeds.domain.respository.ThreeDsAppUrlRepository

internal class ThreeDsAppUrlDataRepository(private val metaDataSource: MetaDataSource) :
    ThreeDsAppUrlRepository {

    override fun getAppUrl(transaction: Transaction): String {
        return Uri.Builder().scheme(PRIMER_THREE_DS_SCHEMA)
            .authority("$PRIMER_THREE_DS_PREFIX${metaDataSource.getApplicationId()}")
            .appendQueryParameter(
                PRIMER_THREE_DS_TRANSACTION_ID_QUERY,
                transaction.authenticationRequestParameters.sdkTransactionID
            )
            .build()
            .toString()
    }

    private companion object {

        const val PRIMER_THREE_DS_SCHEMA = "primer_android_3ds"
        const val PRIMER_THREE_DS_PREFIX = "requestor."
        const val PRIMER_THREE_DS_TRANSACTION_ID_QUERY = "transID"
    }
}
