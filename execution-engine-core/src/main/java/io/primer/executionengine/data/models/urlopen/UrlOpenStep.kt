package io.primer.executionengine.data.models.urlopen

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.core.data.serialization.json.extensions.sequence

internal data class UrlOpenParams(
    val url: String,
    val redirectUrls: List<String>?,
    val webview: WebviewConfig?,
) : JSONDeserializable {

    companion object {
        private const val URL_FIELD = "url"
        private const val REDIRECT_URLS_FIELD = "redirectUrls"
        private const val WEBVIEW_FIELD = "webview"

        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            UrlOpenParams(
                url = json.getString(URL_FIELD),
                redirectUrls = json.optJSONArray(REDIRECT_URLS_FIELD)?.sequence<String>()?.toList(),
                webview = json.optJSONObject(WEBVIEW_FIELD)?.let {
                    WebviewConfig.deserializer.deserialize(it)
                },
            )
        }
    }
}

internal data class WebviewConfig(
    val title: String?,
) : JSONDeserializable {

    companion object {
        private const val TITLE_FIELD = "title"

        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            WebviewConfig(
                title = json.optNullableString(TITLE_FIELD),
            )
        }
    }
}
