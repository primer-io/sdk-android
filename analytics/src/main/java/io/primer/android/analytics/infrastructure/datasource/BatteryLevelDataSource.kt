package io.primer.android.analytics.infrastructure.datasource

import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.os.BatteryManager
import io.primer.android.core.data.datasource.BaseDataSource

internal class BatteryLevelDataSource(private val context: Context) :
    BaseDataSource<Int, Unit> {
    private val batteryManager by lazy {
        context.getSystemService(BATTERY_SERVICE) as BatteryManager
    }

    override fun get() = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}
