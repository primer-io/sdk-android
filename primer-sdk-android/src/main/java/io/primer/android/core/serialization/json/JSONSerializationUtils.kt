package io.primer.android.core.serialization.json

import org.json.JSONObject

internal object JSONSerializationUtils {

    private const val SERIALIZER_FIELD_NAME = "serializer"
    private const val DESERIALIZER_FIELD_NAME = "deserializer"

    @Suppress("UNCHECKED_CAST")
    @Throws(
        IllegalStateException::class,
        NoSuchFieldException::class,
        IllegalAccessException::class
    )
    inline fun <reified T : JSONSerializable> getJsonSerializer(): JSONSerializer<T> {
        val field = T::class.java.getDeclaredField(SERIALIZER_FIELD_NAME)

        check(
            field.type == JSONObjectSerializer::class.java || field.type ==
                JSONArraySerializer::class.java
        ) {
            "Serializer is not of the type JSONSerializer"
        }
        return field[null] as JSONSerializer<T>
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(
        IllegalStateException::class,
        NoSuchFieldException::class,
        IllegalAccessException::class
    )
    inline fun <reified T : JSONObjectSerializable> getJsonObjectSerializer():
        JSONObjectSerializer<T> {
        val field = T::class.java.getDeclaredField(SERIALIZER_FIELD_NAME)
        check(field.type == JSONObjectSerializer::class.java) {
            "Serializer is not of the type JSONObjectSerializable"
        }
        return field[null] as JSONObjectSerializer<T>
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(
        IllegalStateException::class,
        NoSuchFieldException::class,
        IllegalAccessException::class
    )
    inline fun <reified T : JSONDeserializable> getJsonArrayDeserializer():
        JSONArrayDeserializer<T> {
        val field = T::class.java.getDeclaredField(DESERIALIZER_FIELD_NAME)

        check(field.type == JSONArrayDeserializer::class.java) {
            "Deserializer is not of the type JSONArrayDeserializer"
        }
        return field[null] as JSONArrayDeserializer<T>
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(
        IllegalStateException::class,
        NoSuchFieldException::class,
        IllegalAccessException::class
    )
    inline fun <reified T : JSONDeserializable> getJsonObjectDeserializer():
        JSONObjectDeserializer<T> {
        val field = T::class.java.getDeclaredField(DESERIALIZER_FIELD_NAME)
        check(field.type == JSONObjectDeserializer::class.java) {
            "Deserializer is not of the type JSONObjectDeserializer"
        }
        return field[null] as JSONObjectDeserializer<T>
    }

    @Throws(
        IllegalStateException::class,
        NoSuchFieldException::class,
        IllegalAccessException::class
    )
    inline fun <reified T : JSONDeserializable> JSONObject.deserialize(): T {
        return getJsonObjectDeserializer<T>().deserialize(this)
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(
        IllegalStateException::class,
        NoSuchFieldException::class,
        IllegalAccessException::class
    )
    inline fun <reified T : JSONObjectSerializable> T.serialize(): JSONObject {
        val serializer = getJsonObjectSerializer<T>()
        return serializer.serialize(this)
    }
}
