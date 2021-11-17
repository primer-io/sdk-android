package io.primer.android.data.token.datasource

import io.primer.android.data.base.datasource.BaseCacheDataSource
import io.primer.android.data.token.model.ClientToken

internal class LocalClientTokenDataSource(private var clientToken: ClientToken) :
    BaseCacheDataSource<ClientToken, String>() {

    override fun get() = clientToken

    override fun update(input: String) {
        val decodedClientToken = ClientToken.fromString(input)
        if (decodedClientToken.configurationUrl.isNullOrBlank()) {
            this.clientToken =
                decodedClientToken.copy(configurationUrl = this.clientToken.configurationUrl)
        } else {
            this.clientToken = decodedClientToken
        }
    }
}
