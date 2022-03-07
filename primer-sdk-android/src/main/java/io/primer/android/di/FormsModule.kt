package io.primer.android.di

import io.primer.android.data.payments.forms.datasource.LocalFormDataSourceFactory
import io.primer.android.data.payments.forms.repository.FormsDataRepository
import io.primer.android.domain.payments.forms.FormValidationInteractor
import io.primer.android.domain.payments.forms.FormsInteractor
import io.primer.android.domain.payments.forms.repository.FormsRepository
import io.primer.android.domain.payments.forms.validation.ValidatorFactory
import io.primer.android.presentation.payment.forms.FormsViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val formsModule = {
    module {
        single {
            LocalFormDataSourceFactory(get())
        }
        single<FormsRepository> {
            FormsDataRepository(
                get(),
                get()
            )
        }
        single {
            FormsInteractor(
                get(),
            )
        }

        single {
            ValidatorFactory()
        }

        single {
            FormValidationInteractor(get())
        }

        viewModel { FormsViewModel(get(), get(), get(), get()) }
    }
}
