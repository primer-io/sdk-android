package io.primer.android.data.token.repository

import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.token.repository.ClientTokenRepository

internal class ClientTokenDataRepository(
    private val clientTokenDataSource: LocalClientTokenDataSource
) : ClientTokenRepository {

    @Throws(IllegalArgumentException::class)
    override fun setClientToken(clientToken: String) = clientTokenDataSource.setClientToken(
        clientToken
    )

    override fun getClientTokenIntent(): ClientTokenIntent {
        return clientTokenDataSource.getClientToken().intent
    }
}
