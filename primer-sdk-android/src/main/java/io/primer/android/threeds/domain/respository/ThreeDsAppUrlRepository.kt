package io.primer.android.threeds.domain.respository

import com.netcetera.threeds.sdk.api.transaction.Transaction

internal interface ThreeDsAppUrlRepository {

    fun getAppUrl(transaction: Transaction): String?
}
