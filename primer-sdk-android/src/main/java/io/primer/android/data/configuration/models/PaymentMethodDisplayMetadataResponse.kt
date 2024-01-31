package io.primer.android.data.configuration.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableFloat
import io.primer.android.core.serialization.json.extensions.optNullableString
import org.json.JSONObject

internal data class PaymentMethodDisplayMetadataResponse(
    internal val buttonData: ButtonDataResponse
) : JSONDeserializable {

    internal data class ButtonDataResponse(
        val iconUrl: IconUrlDataResponse?,
        val backgroundColorData: ColorDataResponse?,
        val borderColorData: ColorDataResponse?,
        val borderWidthData: BorderWidthDataResponse?,
        val cornerRadius: Float?,
        val text: String?,
        val textColorData: ColorDataResponse?,
        val iconPositionRelativeToText: IconPosition
    ) : JSONDeserializable {
        internal data class IconUrlDataResponse(
            val colored: String?,
            val light: String?,
            val dark: String?
        ) : JSONDeserializable {
            companion object {
                const val COLORED_FIELD = "colored"
                const val LIGHT_FIELD = "light"
                const val DARK_FIELD = "dark"

                @JvmField
                val deserializer = object : JSONObjectDeserializer<IconUrlDataResponse> {

                    override fun deserialize(t: JSONObject): IconUrlDataResponse {
                        return IconUrlDataResponse(
                            t.optNullableString(COLORED_FIELD),
                            t.optNullableString(LIGHT_FIELD),
                            t.optNullableString(DARK_FIELD)
                        )
                    }
                }
            }
        }

        internal data class ColorDataResponse(
            val colored: String?,
            val light: String?,
            val dark: String?
        ) : JSONDeserializable {
            companion object {
                const val COLORED_FIELD = "colored"
                const val LIGHT_FIELD = "light"
                const val DARK_FIELD = "dark"

                @JvmField
                val deserializer = object : JSONObjectDeserializer<ColorDataResponse> {

                    override fun deserialize(t: JSONObject): ColorDataResponse {
                        return ColorDataResponse(
                            t.optNullableString(COLORED_FIELD),
                            t.optNullableString(LIGHT_FIELD),
                            t.optNullableString(DARK_FIELD)
                        )
                    }
                }
            }
        }

        internal data class BorderWidthDataResponse(
            val colored: Float?,
            val light: Float?,
            val dark: Float?
        ) : JSONDeserializable {
            companion object {
                const val COLORED_FIELD = "colored"
                const val LIGHT_FIELD = "light"
                const val DARK_FIELD = "dark"

                @JvmField
                val deserializer = object : JSONObjectDeserializer<BorderWidthDataResponse> {

                    override fun deserialize(t: JSONObject): BorderWidthDataResponse {
                        return BorderWidthDataResponse(
                            t.optNullableFloat(COLORED_FIELD),
                            t.optNullableFloat(LIGHT_FIELD),
                            t.optNullableFloat(DARK_FIELD)
                        )
                    }
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
            val deserializer = object : JSONObjectDeserializer<ButtonDataResponse> {

                override fun deserialize(t: JSONObject): ButtonDataResponse {
                    return ButtonDataResponse(
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
                        IconPosition.safeValueOf(t.optNullableString(ICON_POSITION_FIELD))
                    )
                }
            }
        }
    }

    companion object {
        const val BUTTON_DATA_FIELD = "button"

        @JvmField
        val deserializer = object : JSONObjectDeserializer<PaymentMethodDisplayMetadataResponse> {

            override fun deserialize(t: JSONObject): PaymentMethodDisplayMetadataResponse {
                return PaymentMethodDisplayMetadataResponse(
                    JSONSerializationUtils.getJsonObjectDeserializer<ButtonDataResponse>()
                        .deserialize(t.getJSONObject(BUTTON_DATA_FIELD))
                )
            }
        }
    }
}

internal enum class IconPosition {
    START,
    END,
    ABOVE,
    BELOW;

    companion object {
        fun safeValueOf(value: String?) = values().find { value == it.name }
            ?: START
    }
}
