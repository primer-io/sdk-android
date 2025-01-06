package io.primer.android.analytics.infrastructure.datasource

import android.content.Context
import android.content.pm.PackageManager

internal class MetaDataSource(private val context: Context) {
    private val metaData by lazy {
        context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA,
        ).metaData
    }

    fun getApplicationId(): String = metaData.getString(KEY_APPLICATION_ID, "")

    private companion object {
        const val KEY_APPLICATION_ID = "parentApplicationId"
    }
}
