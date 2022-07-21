package io.primer.android.domain.token.repository

import io.primer.android.data.token.model.ClientTokenIntent

internal interface ClientTokenRepository {

    fun getClientTokenIntent(): ClientTokenIntent

    fun getStatusUrl(): String?

    fun getRedirectUrl(): String?

    fun getQrCode(): String?

    fun getAccountNumber(): String?

    fun getExpiration(): String?

    @Throws(IllegalArgumentException::class)
    fun setClientToken(clientToken: String)
}
