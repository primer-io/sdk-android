package io.primer.android.data.token.repository

import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.domain.token.repository.ClientTokenRepository

internal class ClientTokenDataRepository(
    private val clientTokenDataSource: LocalClientTokenDataSource
) : ClientTokenRepository {

    @Throws(IllegalArgumentException::class)
    override fun setClientToken(clientToken: String) = clientTokenDataSource.update(
        clientToken
    )

    override fun getRedirectUrl() = clientTokenDataSource.get().redirectUrl

    override fun getStatusUrl() = clientTokenDataSource.get().statusUrl

    override fun getBackendCallbackUrl() = clientTokenDataSource.get().backendCallbackUrl

    override fun getTransactionId() = clientTokenDataSource.get().primerTransactionId

    override fun getActionType() = clientTokenDataSource.get().iPay88ActionType

    override fun getPaymentMethodId() = clientTokenDataSource.get().iPay88PaymentMethodId

    override fun getClientTokenIntent() = clientTokenDataSource.get().intent

    override fun useThreeDsWeakValidation() = clientTokenDataSource.get().useThreeDsWeakValidation

    override fun getTransactionNo() = clientTokenDataSource.get().nolPayTransactionNo

    override fun getCompleteUrl() = clientTokenDataSource.get().redirectUrl
}
