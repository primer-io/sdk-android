package io.primer.android.data.configuration.datasource

import io.primer.android.data.configuration.model.Configuration
import io.primer.android.model.dto.PrimerSettings
import kotlinx.coroutines.flow.flowOf

internal class LocalConfigurationDataSource(private val settings: PrimerSettings) {

    private var configuration: Configuration? = null

    fun getConfigurationAsFlow() = flowOf(requireNotNull(configuration))

    fun getConfiguration() = requireNotNull(configuration)

    fun updateConfiguration(configuration: Configuration) {
        this.configuration = configuration
        updateSettings(configuration)
    }

    private fun updateSettings(configuration: Configuration) =
        configuration.clientSession?.apply {
            customer?.let { settings.customer = it }
            order?.let { settings.order = it }
        }
}
