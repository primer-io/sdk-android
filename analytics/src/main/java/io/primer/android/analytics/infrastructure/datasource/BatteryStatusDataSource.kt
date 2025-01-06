package io.primer.android.analytics.infrastructure.datasource

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import io.primer.android.analytics.data.models.BatteryStatus
import io.primer.android.core.data.datasource.BaseDataSource

internal class BatteryStatusDataSource(private val context: Context) :
    BaseDataSource<BatteryStatus, Unit> {
    private val batteryManager by lazy {
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    override fun get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (batteryManager.isCharging) {
                true -> BatteryStatus.CHARGING
                false -> BatteryStatus.NOT_CHARGING
            }
        } else {
            BatteryStatus.UNKNOWN
        }
}
