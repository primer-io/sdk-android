package io.primer.android.analytics.data.models

import android.os.Build
import io.primer.android.analytics.extensions.getMemoryUsage
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import org.json.JSONObject
import java.util.Locale

internal data class DeviceData(
    val batteryLevel: Int,
    val batteryStatus: BatteryStatus,
    val screen: ScreenData,
    val uniqueDeviceIdentifier: String,
    val locale: String = Locale.getDefault().toLanguageTag(),
    val memoryFootprint: Long = Runtime.getRuntime().getMemoryUsage(),
    val modelIdentifier: String = Build.MANUFACTURER,
    val modelName: String = Build.MODEL,
    val platformVersion: String = Build.VERSION.SDK_INT.toString(),
) : JSONSerializable, JSONDeserializable {
    companion object {

        private const val BATTERY_LEVEL_FIELD = "batteryLevel"
        private const val BATTERY_STATUS_FIELD = "batteryStatus"
        private const val SCREEN_DATA_FIELD = "screen"
        private const val UNIQUE_DEVICE_ID_FIELD = "uniqueDeviceIdentifier"
        private const val LOCALE_FIELD = "locale"
        private const val MEMORY_FOOTPRINT_FIELD = "memoryFootprint"
        private const val MODEL_IDENTIFIER_FIELD = "modelIdentifier"
        private const val MODEL_NAME_FIELD = "modelName"
        private const val PLATFORM_VERSION_FIELD = "platformVersion"

        @JvmField
        val serializer = object : JSONSerializer<DeviceData> {
            override fun serialize(t: DeviceData): JSONObject {
                return JSONObject().apply {
                    put(BATTERY_LEVEL_FIELD, t.batteryLevel)
                    put(BATTERY_STATUS_FIELD, t.batteryStatus.name)
                    put(
                        SCREEN_DATA_FIELD,
                        JSONSerializationUtils.getSerializer<ScreenData>()
                            .serialize(t.screen)
                    )
                    put(UNIQUE_DEVICE_ID_FIELD, t.uniqueDeviceIdentifier)
                    put(LOCALE_FIELD, t.locale)
                    put(MEMORY_FOOTPRINT_FIELD, t.memoryFootprint)
                    put(MODEL_IDENTIFIER_FIELD, t.modelIdentifier)
                    put(MODEL_NAME_FIELD, t.modelName)
                    put(PLATFORM_VERSION_FIELD, t.platformVersion)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<DeviceData> {
            override fun deserialize(t: JSONObject): DeviceData {
                return DeviceData(
                    t.getInt(BATTERY_LEVEL_FIELD),
                    BatteryStatus.valueOf(
                        t.getString(BATTERY_STATUS_FIELD)
                    ),
                    JSONSerializationUtils.getDeserializer<ScreenData>()
                        .deserialize(t.getJSONObject(SCREEN_DATA_FIELD)),
                    t.getString(UNIQUE_DEVICE_ID_FIELD),
                    t.getString(LOCALE_FIELD),
                    t.getLong(MEMORY_FOOTPRINT_FIELD),
                    t.getString(MODEL_IDENTIFIER_FIELD),
                    t.getString(MODEL_NAME_FIELD),
                    t.getString(PLATFORM_VERSION_FIELD),
                )
            }
        }
    }
}

internal enum class BatteryStatus {
    CHARGING,
    NOT_CHARGING,
    UNKNOWN
}
