package io.primer.android.di

import io.primer.android.data.deeplink.klarna.KlarnaDeeplinkDataRepository
import io.primer.android.domain.deeplink.klarna.KlarnaDeeplinkInteractor
import io.primer.android.domain.deeplink.klarna.repository.KlarnaDeeplinkRepository
import org.koin.dsl.module

internal val klarnaModule = {
    module {
        single<KlarnaDeeplinkRepository> { KlarnaDeeplinkDataRepository(get()) }
        single { KlarnaDeeplinkInteractor(get()) }
    }
}
