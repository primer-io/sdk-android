package io.primer.android.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

object Serialization {

    private var _json: Json = Json {
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {}
    }
    val json: Json
        get() = _json

    fun addModule(module: SerializersModule) {
        _json = Json(_json) {
            ignoreUnknownKeys = true
            serializersModule = serializersModule.plus(module)
        }
    }
}
