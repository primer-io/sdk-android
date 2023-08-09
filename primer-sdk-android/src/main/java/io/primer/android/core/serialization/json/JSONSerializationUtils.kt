package io.primer.android.core.serialization.json

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
            field.type.equals(JSONObjectSerializer::class.java) || field.type.equals(
                JSONArraySerializer::class.java
            )
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
        check(field.type.equals(JSONObjectSerializer::class.java)) {
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
    inline fun <reified T : JSONDeserializable> getDeserializer(): JSONDeserializer<T> {
        val field = T::class.java.getDeclaredField(DESERIALIZER_FIELD_NAME)
        check(field.type.equals(JSONDeserializer::class.java)) {
            "Deserializer is not of the type JSONDeserializer"
        }
        return field[null] as JSONDeserializer<T>
    }
}
