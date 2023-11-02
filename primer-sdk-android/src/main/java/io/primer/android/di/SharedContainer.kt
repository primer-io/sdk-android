package io.primer.android.di

import android.content.Context
import io.primer.android.analytics.data.datasource.CheckoutSessionIdDataSource
import io.primer.android.analytics.data.datasource.TimerDataSource
import io.primer.android.analytics.data.helper.TimerEventProvider
import io.primer.android.core.logging.internal.DefaultLogReporter
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.data.token.model.ClientToken
import io.primer.android.events.EventDispatcher
import io.primer.android.infrastructure.metadata.datasource.MetaDataSource

internal class SharedContainer(
    private val context: Context,
    private val config: PrimerConfig,
    private val clientToken: ClientToken
) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { context }

        registerSingleton { config }

        registerSingleton { config.settings }

        registerSingleton { CheckoutSessionIdDataSource() }

        registerSingleton { LocalConfigurationDataSource(config.settings) }

        registerSingleton { LocalClientTokenDataSource(clientToken) }

        registerSingleton { TimerEventProvider() }

        registerSingleton { TimerDataSource(resolve()) }

        registerSingleton { MetaDataSource(context) }

        registerSingleton { EventDispatcher() }

        registerSingleton<LogReporter> { DefaultLogReporter() }
    }
}
