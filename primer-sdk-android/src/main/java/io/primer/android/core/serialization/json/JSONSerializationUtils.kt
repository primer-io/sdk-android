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
    inline fun <reified T : JSONDeserializable> getJsonArrayDeserializer():
        JSONArrayDeserializer<T> {
        val field = T::class.java.getDeclaredField(DESERIALIZER_FIELD_NAME)

        check(field.type.equals(JSONArrayDeserializer::class.java)) {
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
        check(field.type.equals(JSONObjectDeserializer::class.java)) {
            "Deserializer is not of the type JSONObjectDeserializer"
        }
        return field[null] as JSONObjectDeserializer<T>
    }
}
