package io.primer.android.threeds.di

import com.netcetera.threeds.sdk.ThreeDS2ServiceInstance
import com.netcetera.threeds.sdk.api.ThreeDS2Service
import io.primer.android.di.DependencyContainer
import io.primer.android.di.SdkContainer
import io.primer.android.logging.DefaultLogger
import io.primer.android.threeds.data.repository.NetceteraThreeDsServiceRepository
import io.primer.android.threeds.data.repository.ThreeDsAppUrlDataRepository
import io.primer.android.threeds.data.repository.ThreeDsConfigurationDataRepository
import io.primer.android.threeds.domain.interactor.DefaultThreeDsInteractor
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.respository.ThreeDsAppUrlRepository
import io.primer.android.threeds.domain.respository.ThreeDsConfigurationRepository
import io.primer.android.threeds.domain.respository.ThreeDsServiceRepository
import io.primer.android.threeds.presentation.ThreeDsViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal class ThreeDsContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<ThreeDS2Service> { ThreeDS2ServiceInstance.get() }

        registerSingleton<ThreeDsServiceRepository> {
            NetceteraThreeDsServiceRepository(
                sdk.resolve(),
                resolve()
            )
        }

        registerFactory(LOGGER_TAG_3DS) { DefaultLogger(LOGGER_TAG_3DS) }

        registerSingleton<ThreeDsConfigurationRepository> {
            ThreeDsConfigurationDataRepository(
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton<ThreeDsAppUrlRepository> { ThreeDsAppUrlDataRepository(sdk.resolve()) }

        registerSingleton<ThreeDsInteractor> {
            DefaultThreeDsInteractor(
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                resolve(LOGGER_TAG_3DS)
            )
        }

        registerFactory { ThreeDsViewModelFactory(resolve(), sdk.resolve(), sdk.resolve()) }
    }

    companion object {
        private const val LOGGER_TAG_3DS = "3DS"
    }
}
