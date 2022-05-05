package io.primer.android.threeds.di

import com.netcetera.threeds.sdk.ThreeDS2ServiceInstance
import io.primer.android.logging.DefaultLogger
import io.primer.android.threeds.data.datasource.Remote3DSAuthDataSource
import io.primer.android.threeds.data.repository.NetceteraThreeDsServiceRepository
import io.primer.android.threeds.data.repository.ThreeDsAppUrlDataRepository
import io.primer.android.threeds.data.repository.ThreeDsConfigurationDataRepository
import io.primer.android.threeds.domain.interactor.DefaultThreeDsInteractor
import io.primer.android.threeds.data.repository.ThreeDsDataRepository
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.respository.ThreeDsAppUrlRepository
import io.primer.android.threeds.domain.respository.ThreeDsConfigurationRepository
import io.primer.android.threeds.domain.respository.ThreeDsServiceRepository
import io.primer.android.threeds.presentation.ThreeDsViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val threeDsModule = module {

    single { Remote3DSAuthDataSource(get()) }

    single<ThreeDsRepository> { ThreeDsDataRepository(get(), get()) }

    single { ThreeDS2ServiceInstance.get() }

    single<ThreeDsServiceRepository> { NetceteraThreeDsServiceRepository(get(), get()) }

    factory { DefaultLogger(LOGGER_TAG_3DS) }

    single<ThreeDsConfigurationRepository> { ThreeDsConfigurationDataRepository(get()) }

    single<ThreeDsAppUrlRepository> { ThreeDsAppUrlDataRepository(get()) }

    single<ThreeDsInteractor> {
        DefaultThreeDsInteractor(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }

    viewModel { ThreeDsViewModel(get(), get(), get()) }
}

private const val LOGGER_TAG_3DS = "3DS"
