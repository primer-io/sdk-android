package io.primer.android.domain.deeplink.async.repository

import kotlinx.coroutines.flow.Flow

internal interface AsyncPaymentMethodDeeplinkRepository {

    fun getDeeplinkUrl(): Flow<String>
}
