package io.primer.android.clientToken.core.token.data.repository

import io.primer.android.clientToken.core.token.data.datasource.CacheClientTokenDataSource
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository

internal class ClientTokenDataRepository(
    private val clientTokenDataSource: CacheClientTokenDataSource,
) : ClientTokenRepository {
    @Throws(IllegalArgumentException::class)
    override fun setClientToken(clientToken: String) =
        clientTokenDataSource.update(
            clientToken,
        )

    override fun getClientTokenIntent() = clientTokenDataSource.get().intent
}
