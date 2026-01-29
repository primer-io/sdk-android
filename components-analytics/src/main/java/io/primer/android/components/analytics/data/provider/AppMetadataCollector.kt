package io.primer.android.components.analytics.data.provider

import android.content.Context
import android.content.pm.PackageManager
import io.primer.android.components.analytics.data.model.AppMetadata

internal class AppMetadataCollector(private val context: Context) {

    fun collect(): AppMetadata {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        val appInfo = packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)

        return AppMetadata(
            appName = packageManager.getApplicationLabel(appInfo).toString(),
            appVersion = packageInfo.versionName ?: "unknown",
            appId = context.packageName,
        )
    }
}
