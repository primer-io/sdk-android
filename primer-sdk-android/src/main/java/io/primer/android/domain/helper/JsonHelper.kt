package io.primer.android.domain.helper

import org.json.JSONObject

internal fun JSONObject.valueBy(key: String): String = this.optString(key)
