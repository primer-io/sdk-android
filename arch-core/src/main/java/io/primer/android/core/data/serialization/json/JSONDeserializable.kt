package io.primer.android.core.data.serialization.json

/**
Interface describing class that can be deserialized from JSON.
Every class implementing this interface should define @JvmField field 'deserializer' in the companion object.
 */
interface JSONDeserializable
