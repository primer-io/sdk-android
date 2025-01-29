package io.primer.android.threeds.data.repository

import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.threeds.BuildConfig
import io.primer.android.threeds.data.exception.ThreeDsUnknownProtocolException
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkProvider
import io.primer.android.threeds.domain.models.ThreeDsAuthParams
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.domain.repository.ThreeDsConfigurationRepository
import io.primer.android.threeds.helpers.ProtocolVersion

internal class ThreeDsConfigurationDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
) : ThreeDsConfigurationRepository {
    override suspend fun getConfiguration() =
        runSuspendCatching {
            configurationDataSource.get()
                .let { configuration ->
                    ThreeDsKeysParams(
                        configuration.environment,
                        configuration.keys?.netceteraApiKey,
                        listOfNotNull(
                            configuration.keys?.threeDSecureIoCertificates,
                            configuration.keys?.threeDsProviderCertificates,
                        ).flatten(),
                    )
                }
        }

    override suspend fun getPreAuthConfiguration(supportedThreeDsProtocolVersions: List<String>) =
        runSuspendCatching {
            ThreeDsAuthParams(
                configurationDataSource.get().environment,
                supportedThreeDsProtocolVersions
                    .mapNotNull { versionNumber ->
                        ProtocolVersion.entries
                            .firstOrNull { protocolVersion ->
                                protocolVersion.versionNumber == versionNumber
                            }
                    }.ifEmpty {
                        throw ThreeDsUnknownProtocolException(
                            initProtocolVersion = supportedThreeDsProtocolVersions.max(),
                            ThreeDsFailureContextParams(
                                threeDsSdkVersion = null,
                                initProtocolVersion = null,
                                threeDsWrapperSdkVersion = BuildConfig.SDK_VERSION_STRING,
                                threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                            ),
                        )
                    },
            )
        }
}
