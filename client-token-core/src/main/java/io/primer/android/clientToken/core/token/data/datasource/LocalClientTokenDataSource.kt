package io.primer.android.clientToken.core.token.data.datasource

import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.clientToken.core.token.data.model.ClientToken

typealias CacheClientTokenDataSource = BaseCacheDataSource<ClientToken, String>

internal class LocalClientTokenDataSource :
    BaseCacheDataSource<ClientToken, String> {

    private var clientToken: ClientToken? = null

    override fun get(): ClientToken {
        return requireNotNull(clientToken)
    }

    override fun update(input: String) {
        val decodedClientToken = ClientToken.fromString(input)
        if (decodedClientToken.configurationUrl.isNullOrBlank()) {
            this.clientToken =
                decodedClientToken.copy(configurationUrl = requireNotNull(this.clientToken).configurationUrl)
        } else {
            this.clientToken = decodedClientToken
        }
    }
}
