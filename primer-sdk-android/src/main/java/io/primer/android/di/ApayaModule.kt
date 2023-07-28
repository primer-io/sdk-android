package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.repository.ApayaSessionConfigurationDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.repository.ApayaTokenizationConfigurationDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.ApayaSessionConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.ApayaTokenizationConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.repository.ApayaSessionConfigurationRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.repository.ApayaTokenizationConfigurationRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.datasource.RemoteApayaDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.repository.ApayaSessionDataRepository
import io.primer.android.domain.payments.apaya.ApayaSessionInteractor
import io.primer.android.domain.payments.apaya.repository.ApayaSessionRepository
import io.primer.android.domain.payments.apaya.validation.ApayaSessionParamsValidator
import io.primer.android.domain.payments.apaya.validation.ApayaWebResultValidator
import io.primer.android.http.PrimerHttpClient
import org.koin.dsl.module

internal val apayaModule = {
    module {
        single { PrimerHttpClient(get()) }
        single { RemoteApayaDataSource(get()) }
        single<ApayaSessionRepository> { ApayaSessionDataRepository(get(), get(), get()) }
        single<ApayaSessionConfigurationRepository> {
            ApayaSessionConfigurationDataRepository(
                get(),
                get()
            )
        }
        single<ApayaTokenizationConfigurationRepository> {
            ApayaTokenizationConfigurationDataRepository(
                get(),
                get()
            )
        }

        single { ApayaSessionParamsValidator() }
        single { ApayaWebResultValidator() }
        single { ApayaSessionInteractor(get(), get(), get(), get()) }
        single {
            ApayaSessionConfigurationInteractor(
                get(),
                get()
            )
        }
        single {
            ApayaTokenizationConfigurationInteractor(
                get(),
                get()
            )
        }
    }
}
