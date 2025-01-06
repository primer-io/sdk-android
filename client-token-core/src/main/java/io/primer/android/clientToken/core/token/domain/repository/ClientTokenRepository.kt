package io.primer.android.clientToken.core.token.domain.repository

interface ClientTokenRepository {
    fun getClientTokenIntent(): String

    @Throws(IllegalArgumentException::class)
    fun setClientToken(clientToken: String)
}
