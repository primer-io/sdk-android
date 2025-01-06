package io.primer.android.components.di

import io.primer.android.components.assets.DefaultAssetsHeadlessDelegate
import io.primer.android.components.assets.displayMetadata.PaymentMethodsImplementationInteractor
import io.primer.android.components.assets.displayMetadata.displayMetadata.repository.PaymentMethodImplementationDataRepository
import io.primer.android.components.assets.displayMetadata.repository.PaymentMethodImplementationRepository
import io.primer.android.components.assets.validation.resolvers.AssetManagerInitValidationRulesResolver
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer

internal class AssetManagerContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton<PaymentMethodImplementationRepository> {
            PaymentMethodImplementationDataRepository(
                sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                sdk().resolve(),
            )
        }

        registerSingleton {
            PaymentMethodsImplementationInteractor(resolve(), sdk().resolve())
        }

        registerFactory {
            AssetManagerInitValidationRulesResolver(sdk().resolve())
        }

        registerFactory {
            DefaultAssetsHeadlessDelegate(
                resolve(),
                sdk().resolve(),
                sdk().resolve(),
                sdk().resolve(),
                sdk().resolve(),
            )
        }
    }
}
