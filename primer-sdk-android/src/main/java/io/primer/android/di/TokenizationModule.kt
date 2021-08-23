package io.primer.android.di

import io.primer.android.data.tokenization.repository.TokenizationDataRepository
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.repository.TokenizationRepository
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator
import org.koin.dsl.module

internal val tokenizationModule = {
    module {
        single { ThreeDsSdkClassValidator() }
        single<TokenizationRepository> {
            TokenizationDataRepository(
                get()
            )
        }
        single {
            TokenizationInteractor(
                get(),
                get(),
                get(),
                get()
            )
        }
    }
}
