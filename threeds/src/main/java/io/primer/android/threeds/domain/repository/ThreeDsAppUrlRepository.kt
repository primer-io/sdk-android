package io.primer.android.threeds.domain.repository

import com.netcetera.threeds.sdk.api.transaction.Transaction

internal interface ThreeDsAppUrlRepository {
    fun getAppUrl(transaction: Transaction): String?
}
