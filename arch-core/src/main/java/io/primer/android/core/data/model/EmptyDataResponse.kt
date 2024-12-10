package io.primer.android.core.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer

class EmptyDataResponse : JSONDeserializable {

    companion object {

        @JvmField
        val deserializer = JSONObjectDeserializer { EmptyDataResponse() }
    }
}
