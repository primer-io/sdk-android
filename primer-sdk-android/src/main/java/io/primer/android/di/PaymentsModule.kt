package io.primer.android.di

import io.primer.android.domain.payments.create.CreatePaymentInteractor
import io.primer.android.domain.payments.resume.ResumePaymentInteractor
import io.primer.android.data.payments.create.datasource.CreatePaymentsDataSource
import io.primer.android.data.payments.resume.datasource.ResumePaymentDataSource
import io.primer.android.data.payments.create.repository.CreatePaymentsDataRepository
import io.primer.android.data.payments.resume.repository.ResumePaymentDataRepository
import io.primer.android.domain.payments.helpers.PaymentResultEventsResolver
import io.primer.android.domain.payments.create.repository.CreatePaymentsRepository
import io.primer.android.domain.payments.resume.respository.ResumePaymentsRepository
import org.koin.dsl.module

internal val PaymentsModule = {
    module {
        single { CreatePaymentsDataSource(get()) }
        single { ResumePaymentDataSource(get()) }

        single<CreatePaymentsRepository> {
            CreatePaymentsDataRepository(
                get(),
                get(),
            )
        }

        single<ResumePaymentsRepository> {
            ResumePaymentDataRepository(
                get(),
                get(),
            )
        }

        factory { PaymentResultEventsResolver(get()) }
        factory { ResumePaymentInteractor(get(), get(), get()) }

        factory {
            CreatePaymentInteractor(
                get(),
                get(),
                get(),
            )
        }
    }
}
