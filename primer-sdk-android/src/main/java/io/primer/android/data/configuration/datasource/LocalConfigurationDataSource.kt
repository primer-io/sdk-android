package io.primer.android.data.configuration.datasource

import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.configuration.models.Configuration
import io.primer.android.data.configuration.exception.MissingConfigurationException
import io.primer.android.data.settings.PrimerSettings
import kotlinx.coroutines.flow.flowOf

internal class LocalConfigurationDataSource(private val settings: PrimerSettings) :
    BaseFlowCacheDataSource<Configuration, Configuration> {

    private var configuration: Configuration? = null

    @Throws(MissingConfigurationException::class)
    override fun get() = try {
        flowOf(requireNotNull(configuration))
    } catch (e: IllegalArgumentException) {
        throw MissingConfigurationException(e)
    }

    override fun update(input: Configuration) {
        this.configuration = input
        updateSettings(input)
    }

    fun getConfiguration() = requireNotNull(configuration)

    fun getConfigurationNullable() = configuration

    private fun updateSettings(configuration: Configuration) =
        configuration.clientSession?.apply {
            customer?.let { settings.customer = it }
            order?.let { settings.order = it }
        }
}
