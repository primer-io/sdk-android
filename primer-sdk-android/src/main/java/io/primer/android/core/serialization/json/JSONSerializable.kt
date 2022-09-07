package io.primer.android.core.serialization.json

/**
Interface describing class that can be serialized to JSON.
Every class implementing this interface should define @JvmField field 'serializer' in the companion object.
 */
internal interface JSONSerializable
