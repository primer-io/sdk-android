package io.primer.android.analytics.infrastructure.datasource

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import io.primer.android.core.data.datasource.BaseDataSource
import java.util.UUID

internal data class DeviceIdDataSource(private val context: Context) : BaseDataSource<String, Unit> {
    @SuppressLint("HardwareIds")
    override fun get() =
        Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        ) ?: UUID.randomUUID().toString()
}
