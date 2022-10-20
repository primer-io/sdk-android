package io.primer.android.di

import io.primer.android.data.payments.create.datasource.CreatePaymentDataSource
import io.primer.android.data.payments.create.datasource.LocalPaymentDataSource
import io.primer.android.data.payments.create.repository.CreatePaymentDataRepository
import io.primer.android.data.payments.create.repository.PaymentResultDataRepository
import io.primer.android.data.payments.resume.datasource.ResumePaymentDataSource
import io.primer.android.data.payments.resume.repository.ResumePaymentDataRepository
import io.primer.android.data.tokenization.helper.PrimerPaymentMethodDataHelper
import io.primer.android.domain.payments.create.CreatePaymentInteractor
import io.primer.android.domain.payments.create.repository.CreatePaymentRepository
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.helpers.PaymentResultEventsResolver
import io.primer.android.domain.payments.resume.ResumePaymentInteractor
import io.primer.android.domain.payments.resume.respository.ResumePaymentsRepository
import org.koin.dsl.module

internal val PaymentsModule = {
    module {
        single { CreatePaymentDataSource(get()) }
        single { LocalPaymentDataSource() }
        single { ResumePaymentDataSource(get()) }
        single { PrimerPaymentMethodDataHelper(get(), get(), getScope(RETAIL_OUTLET_SCOPE).get()) }

        single<CreatePaymentRepository> {
            CreatePaymentDataRepository(
                get(),
                get(),
                get(),
                get(),
            )
        }

        single<PaymentResultRepository> {
            PaymentResultDataRepository(
                get(),
            )
        }

        single<ResumePaymentsRepository> {
            ResumePaymentDataRepository(
                get(),
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
