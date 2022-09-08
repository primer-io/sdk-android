package io.primer.android.analytics

import android.content.Context
import androidx.startup.Initializer
import io.primer.android.HttpClientFactory
import io.primer.android.analytics.data.datasource.LocalAnalyticsDataSource
import io.primer.android.analytics.infrastructure.datasource.FileAnalyticsDataSource
import io.primer.android.analytics.data.datasource.RemoteAnalyticsDataSource
import io.primer.android.analytics.data.helper.AnalyticsDataSender
import io.primer.android.analytics.infrastructure.files.AnalyticsFileProvider
import io.primer.android.analytics.data.repository.AnalyticsInitDataRepository
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class AnalyticsInitializer : Initializer<AnalyticsInitDataRepository> {

    override fun create(context: Context): AnalyticsInitDataRepository {
        val fileAnalyticsDataSource =
            FileAnalyticsDataSource(AnalyticsFileProvider(context))
        val remoteAnalyticsFlowDataSource = RemoteAnalyticsDataSource(
            PrimerHttpClient(
                HttpClientFactory().build(),
            )
        )
        return AnalyticsInitDataRepository(
            AnalyticsDataSender(
                remoteAnalyticsFlowDataSource,
                LocalAnalyticsDataSource.instance,
                fileAnalyticsDataSource
            ),
            LocalAnalyticsDataSource.instance,
            fileAnalyticsDataSource
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }
}
