package io.primer.android.domain.token.repository

internal interface ClientTokenRepository {

    fun getClientTokenIntent(): String

    fun getStatusUrl(): String?

    fun getRedirectUrl(): String?

    fun getBackendCallbackUrl(): String?

    fun getTransactionId(): String?

    @Throws(IllegalArgumentException::class)
    fun setClientToken(clientToken: String)
}
