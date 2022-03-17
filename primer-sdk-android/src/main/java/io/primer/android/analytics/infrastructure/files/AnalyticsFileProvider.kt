package io.primer.android.analytics.infrastructure.files

import android.content.Context
import java.io.File

internal class AnalyticsFileProvider(private val context: Context) {

    fun getFile(path: String): File {
        val directory = File(context.filesDir, ANALYTICS_DIRECTORY)
        if (!directory.exists()) directory.mkdirs()
        val file = File(
            context.filesDir,
            ANALYTICS_DIRECTORY + File.pathSeparator + path
        )
        if (!file.exists()) file.createNewFile()
        return file
    }

    companion object {
        const val ANALYTICS_DIRECTORY = "primer-sdk"
        const val ANALYTICS_EVENTS_PATH = "analytics-events"
    }
}
