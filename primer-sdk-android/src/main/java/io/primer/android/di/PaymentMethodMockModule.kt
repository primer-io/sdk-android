package io.primer.android.di

import io.primer.android.data.mock.datasource.RemoteFinalizeMockedFlowDataSource
import io.primer.android.data.mock.repository.MockDataConfigurationRepository
import io.primer.android.domain.mock.FinaliseMockedFlowInteractor
import io.primer.android.domain.mock.MockConfigurationInteractor
import io.primer.android.domain.mock.repository.MockConfigurationRepository
import io.primer.android.presentation.mock.PaymentMethodMockViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val paymentMethodMockModule = {
    module {
        single {
            RemoteFinalizeMockedFlowDataSource(get())
        }

        single<MockConfigurationRepository> { MockDataConfigurationRepository(get(), get()) }

        single {
            MockConfigurationInteractor(
                get(),
            )
        }

        single {
            FinaliseMockedFlowInteractor(get())
        }

        viewModel { PaymentMethodMockViewModel(get(), get()) }
    }
}
