package io.primer.android.threeds.data.repository

import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.exception.MissingConfigurationException
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.threeds.data.exception.ThreeDsUnknownProtocolException
import io.primer.android.threeds.domain.models.ThreeDsAuthParams
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.domain.respository.ThreeDsConfigurationRepository
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class ThreeDsConfigurationDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val clientTokenDataSource: LocalClientTokenDataSource
) : ThreeDsConfigurationRepository {

    override fun getConfiguration() = try {
        localConfigurationDataSource.get()
            .map { configuration ->
                ThreeDsKeysParams(
                    configuration.environment,
                    configuration.keys?.netceteraApiKey,
                    configuration.keys?.threeDSecureIoCertificates
                )
            }
    } catch (e: MissingConfigurationException) {
        flow { throw e }
    }

    override fun getPreAuthConfiguration() = localConfigurationDataSource.get()
        .map { configuration ->
            val supportedThreeDsProtocolVersions =
                clientTokenDataSource.get().supportedThreeDsProtocolVersions
            ThreeDsAuthParams(
                configuration.environment,
                supportedThreeDsProtocolVersions
                    ?.mapNotNull { versionNumber ->
                        ProtocolVersion.values()
                            .firstOrNull { protocolVersion ->
                                protocolVersion.versionNumber == versionNumber
                            }
                    }.orEmpty().ifEmpty {
                        throw ThreeDsUnknownProtocolException(
                            supportedThreeDsProtocolVersions.orEmpty().max(),
                            ThreeDsFailureContextParams(null, null)
                        )
                    }
            )
        }
}
