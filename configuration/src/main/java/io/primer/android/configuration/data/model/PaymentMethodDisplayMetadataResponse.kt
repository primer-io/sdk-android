package io.primer.android.configuration.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableFloat
import io.primer.android.core.data.serialization.json.extensions.optNullableString

data class PaymentMethodDisplayMetadataResponse(
    val buttonData: ButtonDataResponse,
) : JSONDeserializable {
    data class ButtonDataResponse(
        val iconUrl: IconUrlDataResponse?,
        val backgroundColorData: ColorDataResponse?,
        val borderColorData: ColorDataResponse?,
        val borderWidthData: BorderWidthDataResponse?,
        val cornerRadius: Float?,
        val text: String?,
        val textColorData: ColorDataResponse?,
        val iconPositionRelativeToText: IconPosition,
    ) : JSONDeserializable {
        data class IconUrlDataResponse(
            val colored: String?,
            val light: String?,
            val dark: String?,
        ) : JSONDeserializable {
            companion object {
                const val COLORED_FIELD = "colored"
                const val LIGHT_FIELD = "light"
                const val DARK_FIELD = "dark"

                @JvmField
                val deserializer =
                    JSONObjectDeserializer { t ->
                        IconUrlDataResponse(
                            t.optNullableString(COLORED_FIELD),
                            t.optNullableString(LIGHT_FIELD),
                            t.optNullableString(DARK_FIELD),
                        )
                    }
            }
        }

        data class ColorDataResponse(
            val colored: String?,
            val light: String?,
            val dark: String?,
        ) : JSONDeserializable {
            companion object {
                const val COLORED_FIELD = "colored"
                const val LIGHT_FIELD = "light"
                const val DARK_FIELD = "dark"

                @JvmField
                val deserializer =
                    JSONObjectDeserializer { t ->
                        ColorDataResponse(
                            t.optNullableString(COLORED_FIELD),
                            t.optNullableString(LIGHT_FIELD),
                            t.optNullableString(DARK_FIELD),
                        )
                    }
            }
        }

        data class BorderWidthDataResponse(
            val colored: Float?,
            val light: Float?,
            val dark: Float?,
        ) : JSONDeserializable {
            companion object {
                const val COLORED_FIELD = "colored"
                const val LIGHT_FIELD = "light"
                const val DARK_FIELD = "dark"

                @JvmField
                val deserializer =
                    JSONObjectDeserializer { t ->
                        BorderWidthDataResponse(
                            t.optNullableFloat(COLORED_FIELD),
                            t.optNullableFloat(LIGHT_FIELD),
                            t.optNullableFloat(DARK_FIELD),
                        )
                    }
            }
        }

        companion object {
            const val ICON_URL_DATA_FIELD = "iconUrl"
            const val BACKGROUND_COLOR_DATA_FIELD = "backgroundColor"
            const val BORDER_COLOR_DATA_FIELD = "borderColor"
            const val BORDER_WIDTH_DATA_FIELD = "borderWidth"
            const val CORNER_RADIUS_FIELD = "cornerRadius"
            const val TEXT_FIELD = "text"
            const val TEXT_COLOR_DATA_FIELD = "textColor"
            const val ICON_POSITION_FIELD = "iconPositionRelativeToText"

            @JvmField
            val deserializer =
                JSONObjectDeserializer { t ->
                    ButtonDataResponse(
                        t.optJSONObject(ICON_URL_DATA_FIELD)?.let {
                            JSONSerializationUtils.getJsonObjectDeserializer<IconUrlDataResponse>()
                                .deserialize(it)
                        },
                        t.optJSONObject(BACKGROUND_COLOR_DATA_FIELD)?.let {
                            JSONSerializationUtils.getJsonObjectDeserializer<ColorDataResponse>()
                                .deserialize(it)
                        },
                        t.optJSONObject(BORDER_COLOR_DATA_FIELD)?.let {
                            JSONSerializationUtils.getJsonObjectDeserializer<ColorDataResponse>()
                                .deserialize(it)
                        },
                        t.optJSONObject(BORDER_WIDTH_DATA_FIELD)?.let {
                            JSONSerializationUtils
                                .getJsonObjectDeserializer<BorderWidthDataResponse>()
                                .deserialize(it)
                        },
                        t.optNullableFloat(CORNER_RADIUS_FIELD),
                        t.optNullableString(TEXT_FIELD),
                        t.optJSONObject(TEXT_COLOR_DATA_FIELD)?.let {
                            JSONSerializationUtils.getJsonObjectDeserializer<ColorDataResponse>()
                                .deserialize(it)
                        },
                        IconPosition.safeValueOf(t.optNullableString(ICON_POSITION_FIELD)),
                    )
                }
        }
    }

    companion object {
        const val BUTTON_DATA_FIELD = "button"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                PaymentMethodDisplayMetadataResponse(
                    JSONSerializationUtils.getJsonObjectDeserializer<ButtonDataResponse>()
                        .deserialize(t.getJSONObject(BUTTON_DATA_FIELD)),
                )
            }
    }
}

enum class IconPosition {
    START,
    END,
    ABOVE,
    BELOW,
    ;

    companion object {
        fun safeValueOf(value: String?) =
            IconPosition.entries.find { value == it.name }
                ?: START
    }
}
