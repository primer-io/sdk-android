package io.primer.android.components.analytics.data.provider

import android.os.Build
import io.primer.android.analytics.infrastructure.datasource.connectivity.ConnectivityProvider
import io.primer.android.analytics.infrastructure.datasource.connectivity.toNetworkType
import io.primer.android.components.analytics.data.model.DeviceInfo
import java.util.Locale
import java.util.TimeZone

internal class DeviceInfoCollector(
    private val connectivityProvider: ConnectivityProvider,
) {

    fun collect(): DeviceInfo {
        return DeviceInfo(
            model = getDeviceModel(),
            osVersion = Build.VERSION.RELEASE,
            locale = Locale.getDefault().toString(),
            timezone = TimeZone.getDefault().id,
            networkType = getNetworkType(),
        )
    }

    private fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    private fun getNetworkType(): String {
        return connectivityProvider.getNetworkState().toNetworkType().name.lowercase()
    }
}
