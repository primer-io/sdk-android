package io.primer.android.data.mock.repository

import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.domain.mock.repository.ConfigurationMockRepository

internal class ConfigurationMockDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource
) :
    ConfigurationMockRepository {

    override fun isMockedFlow() =
        localConfigurationDataSource.getConfiguration().clientSession?.testId.isNullOrBlank().not()
}
