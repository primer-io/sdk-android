package io.primer.android.di

import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.tokenization.repository.TokenizationDataRepository
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.repository.TokenizationRepository
import io.primer.android.logging.DefaultLogger
import io.primer.android.logging.Logger
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val RESUME_HANDLER_LOGGER_NAME = "RESUME_HANDLER"

internal val tokenizationModule = {
    module {
        single { ThreeDsSdkClassValidator() }

        factory<Logger>(named(RESUME_HANDLER_LOGGER_NAME)) {
            DefaultLogger(
                RESUME_HANDLER_LOGGER_NAME
            )
        }

        factory {
            ResumeHandlerFactory(
                get(),
                get(),
                get(),
                get(),
                get(named(RESUME_HANDLER_LOGGER_NAME))
            )
        }

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
                get(),
                get()
            )
        }
        viewModel { TokenizationViewModel(get(), get(), get(), get(), get(), get()) }
    }
}
