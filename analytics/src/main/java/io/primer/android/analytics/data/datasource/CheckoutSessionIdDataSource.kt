package io.primer.android.analytics.data.datasource

import io.primer.android.core.utils.BaseDataProvider
import java.util.UUID

typealias CheckoutSessionIdProvider = BaseDataProvider<String>

internal class CheckoutSessionIdDataSource : BaseDataProvider<String> {

    private val checkoutSessionId by lazy { UUID.randomUUID().toString() }
    override fun provide(): String {
        return checkoutSessionId
    }
}
