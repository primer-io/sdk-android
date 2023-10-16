package io.primer.android.core.data.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer

internal class EmptyDataResponse : JSONDeserializable {

    companion object {
        @JvmField
        val deserializer = JSONDeserializer { EmptyDataResponse() }
    }
}
