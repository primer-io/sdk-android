package io.primer.android.data.token.datasource

import io.primer.android.data.token.model.ClientToken

internal class LocalClientTokenDataSource(private var clientToken: ClientToken) {

    fun getClientToken() = clientToken

    fun setClientToken(clientToken: String) {
        val decodedClientToken = ClientToken.fromString(clientToken)
        if (decodedClientToken.configurationUrl.isNullOrBlank()) {
            this.clientToken =
                decodedClientToken.copy(configurationUrl = this.clientToken.configurationUrl)
        } else {
            this.clientToken = decodedClientToken
        }
    }
}
