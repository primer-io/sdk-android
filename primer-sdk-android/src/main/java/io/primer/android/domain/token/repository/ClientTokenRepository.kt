package io.primer.android.domain.token.repository

internal interface ClientTokenRepository {

    fun getClientTokenIntent(): String

    fun getStatusUrl(): String?

    fun getRedirectUrl(): String?

    fun getBackendCallbackUrl(): String?

    fun getTransactionId(): String?

    fun getActionType(): String?

    fun getPaymentMethodId(): String?

    fun useThreeDsWeakValidation(): Boolean?

    fun getTransactionNo(): String?

    fun getCompleteUrl(): String?

    @Throws(IllegalArgumentException::class)
    fun setClientToken(clientToken: String)
}
