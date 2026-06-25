package io.primer.executionengine.data.models.urlopen

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.extensions.sequence

internal data class UrlOpenParams(
    val url: String,
    val redirectUrls: List<String>?,
) : JSONDeserializable {

    companion object {
        private const val URL_FIELD = "url"
        private const val REDIRECT_URLS_FIELD = "redirectUrls"

        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            UrlOpenParams(
                url = json.getString(URL_FIELD),
                redirectUrls = json.optJSONArray(REDIRECT_URLS_FIELD)?.sequence<String>()?.toList(),
            )
        }
    }
}
