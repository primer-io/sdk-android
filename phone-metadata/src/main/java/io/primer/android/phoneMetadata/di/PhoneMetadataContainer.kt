package io.primer.android.phoneMetadata.di

import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.phoneMetadata.data.datasource.RemotePhoneMetadataDataSource
import io.primer.android.phoneMetadata.data.repository.PhoneMetadataDataRepository
import io.primer.android.phoneMetadata.domain.repository.PhoneMetadataRepository

class PhoneMetadataContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton {
            RemotePhoneMetadataDataSource(
                httpClient = sdk().resolve(),
            )
        }

        registerFactory<PhoneMetadataRepository> {
            PhoneMetadataDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                remoteMetadataDataSource = resolve(),
            )
        }
    }
}
