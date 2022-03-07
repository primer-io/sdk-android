package io.primer.android.analytics.data.models

import android.os.Build
import io.primer.android.analytics.extensions.getMemoryUsage
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
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
)

internal enum class BatteryStatus {
    CHARGING,
    NOT_CHARGING,
    UNKNOWN
}
