package io.primer.android.di

import io.primer.android.data.payments.apaya.datasource.RemoteApayaDataSource
import io.primer.android.data.payments.apaya.repository.ApayaDataRepository
import io.primer.android.domain.payments.apaya.ApayaInteractor
import io.primer.android.domain.payments.apaya.repository.ApayaRepository
import io.primer.android.domain.payments.apaya.validation.ApayaSessionParamsValidator
import io.primer.android.domain.payments.apaya.validation.ApayaWebResultValidator
import io.primer.android.http.PrimerHttpClient
import org.koin.dsl.module

internal val apayaModule = {
    module {
        single { PrimerHttpClient(get(), get()) }
        single { RemoteApayaDataSource(get()) }
        single<ApayaRepository> { ApayaDataRepository(get(), get()) }
        single { ApayaSessionParamsValidator() }
        single { ApayaWebResultValidator() }
        single { ApayaInteractor(get(), get(), get(), get()) }
    }
}
