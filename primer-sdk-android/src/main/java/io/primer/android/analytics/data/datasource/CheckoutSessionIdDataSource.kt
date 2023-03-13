package io.primer.android.analytics.data.datasource

import java.util.UUID

internal class CheckoutSessionIdDataSource {

    val checkoutSessionId by lazy { UUID.randomUUID().toString() }
}
