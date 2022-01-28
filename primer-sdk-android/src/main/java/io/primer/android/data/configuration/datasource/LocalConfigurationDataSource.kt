package io.primer.android.data.configuration.datasource

import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.configuration.model.Configuration
import io.primer.android.model.dto.PrimerSettings
import kotlinx.coroutines.flow.flowOf

internal class LocalConfigurationDataSource(private val settings: PrimerSettings) :
    BaseFlowCacheDataSource<Configuration, Configuration> {

    private var configuration: Configuration? = null

    override fun get() = flowOf(requireNotNull(configuration))

    override fun update(input: Configuration) {
        this.configuration = input
        updateSettings(input)
    }

    fun getConfiguration() = requireNotNull(configuration)

    private fun updateSettings(configuration: Configuration) =
        configuration.clientSession?.apply {
            customer?.let { settings.customer = it }
            order?.let { settings.order = it }
        }
}
