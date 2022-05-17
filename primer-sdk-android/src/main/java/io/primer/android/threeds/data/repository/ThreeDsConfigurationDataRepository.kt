package io.primer.android.threeds.data.repository

import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.Environment
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.domain.respository.ThreeDsConfigurationRepository
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.flow.map

internal class ThreeDsConfigurationDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : ThreeDsConfigurationRepository {

    override fun getConfiguration() =
        localConfigurationDataSource.get()
            .map {
                ThreeDsKeysParams(
                    it.environment,
                    it.keys?.netceteraLicenseKey,
                    it.keys?.threeDSecureIoCertificates
                )
            }

    override fun getProtocolVersion() = localConfigurationDataSource.get()
        .map {
            when (it.environment) {
                Environment.PRODUCTION -> ProtocolVersion.V_210
                else -> ProtocolVersion.V_220
            }
        }
}
