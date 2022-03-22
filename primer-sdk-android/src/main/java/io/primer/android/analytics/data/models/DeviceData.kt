package io.primer.android.analytics.data.models

import android.os.Build
import io.primer.android.analytics.extensions.getMemoryUsage
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
internal data class DeviceData(
    val batteryLevel: Int,
    val batteryStatus: BatteryStatus,
    val screen: ScreenData,
    val uniqueDeviceIdentifier: String,
    @EncodeDefault val locale: String = Locale.getDefault().toLanguageTag(),
    @EncodeDefault val memoryFootprint: Long = Runtime.getRuntime().getMemoryUsage(),
    @EncodeDefault val modelIdentifier: String = Build.MANUFACTURER,
    @EncodeDefault val modelName: String = Build.MODEL,
    @EncodeDefault val platformVersion: String = Build.VERSION.SDK_INT.toString(),
)

internal enum class BatteryStatus {
    CHARGING,
    NOT_CHARGING,
    UNKNOWN
}
