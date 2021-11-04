package io.primer.android.di

import io.primer.android.completion.AsyncPaymentResumeHandler
import io.primer.android.completion.DefaultResumeHandler
import io.primer.android.completion.ResumeHandler
import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.completion.ThreeDsResumeHandler
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

private const val THREE_DS_RESUME_HANDLER_NAME = "THREE_DS_RESUME_HANDLER"
private const val DEFAULT_RESUME_HANDLER_NAME = "DEFAULT_RESUME_HANDLER"
private const val RESUME_HANDLER_LOGGER_NAME = "RESUME_HANDLER"
private const val ASYNC_PAYMENT_RESUME_HANDLER_NAME = "ASYNC_PAYMENT_RESUME_HANDLER_NAME"

internal val tokenizationModule = {
    module {
        single { ThreeDsSdkClassValidator() }

        factory<Logger>(named(RESUME_HANDLER_LOGGER_NAME)) {
            DefaultLogger(
                RESUME_HANDLER_LOGGER_NAME
            )
        }

        factory<ResumeHandler>(named(DEFAULT_RESUME_HANDLER_NAME)) {
            DefaultResumeHandler(
                get(),
                get(),
                get(),
                get(named(RESUME_HANDLER_LOGGER_NAME))
            )
        }

        factory<ResumeHandler>(named(THREE_DS_RESUME_HANDLER_NAME)) {
            ThreeDsResumeHandler(
                get(),
                get(),
                get(),
                get(),
                get(named(RESUME_HANDLER_LOGGER_NAME))
            )
        }

        factory<ResumeHandler>(named(ASYNC_PAYMENT_RESUME_HANDLER_NAME)) {
            AsyncPaymentResumeHandler(
                get(),
                get(),
                get(),
                get(named(RESUME_HANDLER_LOGGER_NAME))
            )
        }

        factory {
            ResumeHandlerFactory(
                get(named(THREE_DS_RESUME_HANDLER_NAME)),
                get(named(ASYNC_PAYMENT_RESUME_HANDLER_NAME)),
                get(named(DEFAULT_RESUME_HANDLER_NAME))
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
        viewModel { TokenizationViewModel(get(), get(), get(), get()) }
    }
}
