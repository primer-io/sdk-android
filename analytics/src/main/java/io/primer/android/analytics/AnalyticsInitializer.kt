package io.primer.android.analytics

import android.content.Context
import androidx.startup.Initializer
import io.primer.android.analytics.data.datasource.LocalAnalyticsDataSource
import io.primer.android.analytics.data.datasource.RemoteAnalyticsDataSource
import io.primer.android.analytics.data.helper.AnalyticsDataSender
import io.primer.android.analytics.data.network.HttpClientFactory
import io.primer.android.analytics.data.repository.AnalyticsInitDataRepository
import io.primer.android.analytics.di.AnalyticsContainer
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.infrastructure.datasource.FileAnalyticsDataSource
import io.primer.android.analytics.infrastructure.files.AnalyticsFileProvider
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.logging.di.HttpLogObfuscationContainer
import io.primer.android.core.logging.internal.DefaultLogReporter
import io.primer.android.core.logging.internal.LogReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
internal class AnalyticsInitializer : Initializer<AnalyticsInitDataRepository>, DISdkComponent {
    override fun create(context: Context): AnalyticsInitDataRepository {
        initializeCoreContainer(context = context)
        CoroutineScope(Dispatchers.IO).launch {
            resolve<AnalyticsInteractor>().startObservingEvents()
        }
        val fileAnalyticsDataSource =
            FileAnalyticsDataSource(AnalyticsFileProvider(context))
        val remoteAnalyticsFlowDataSource = RemoteAnalyticsDataSource(
            PrimerHttpClient(
                HttpClientFactory(
                    logReporter = resolve(),
                    blacklistedHttpHeaderProviderRegistry = resolve(),
                    whitelistedHttpBodyKeyProviderRegistry = resolve(),
                    pciUrlProvider = resolve(AnalyticsContainer.PCI_URL_PROVIDER_INTERNAL_DI_KEY)
                ).build(),
                logProvider = { MutableStateFlow(value = null) },
                messagePropertiesEventProvider = { MutableStateFlow(value = null) }
            )
        )

        return AnalyticsInitDataRepository(
            AnalyticsDataSender(remoteAnalyticsFlowDataSource),
            LocalAnalyticsDataSource.instance,
            fileAnalyticsDataSource
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }

    private fun initializeCoreContainer(context: Context) {
        DISdkContext.coreContainer = SdkContainer().apply {
            registerContainer(object : DependencyContainer() {
                override fun registerInitialDependencies() {
                    registerSingleton { context.applicationContext }
                    registerSingleton<LogReporter> { DefaultLogReporter() }
                }
            })
            registerContainer(HttpLogObfuscationContainer())
        }
        DISdkContext.coreContainer?.registerContainer(AnalyticsContainer { getSdkContainer() })
    }
}
