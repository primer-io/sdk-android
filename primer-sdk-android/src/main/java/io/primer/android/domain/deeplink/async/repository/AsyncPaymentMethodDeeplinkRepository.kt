package io.primer.android.domain.deeplink.async.repository

internal interface AsyncPaymentMethodDeeplinkRepository {

    fun getDeeplinkUrl(): String
}
