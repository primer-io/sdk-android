package io.primer.android.session

import android.util.Base64
import org.json.JSONObject

class ClientToken(token: String) {
    private var decoded = getTokenPayload(token)

    val configurationURL: String
        get() = this.decoded.getString("configurationUrl")

    val coreURL: String
        get() = this.decoded.getString("coreUrl")

    val pciURL: String
        get() = this.decoded.getString("pciUrl")

    val accessToken: String
        get() = this.decoded.getString("accessToken")

    private fun getTokenPayload(token: String): JSONObject {
        val tokens = token.split(".")

        for (elm in tokens) {
            val bytes = Base64.decode(elm, Base64.DEFAULT)
            val decoded = String(bytes)

            if (decoded.contains("\"accessToken\":")) {
                return JSONObject(decoded)
            }
        }

        return JSONObject("null")
    }
}