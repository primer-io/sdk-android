@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.di

import io.primer.android.data.payments.create.datasource.CreatePaymentDataSource
import io.primer.android.data.payments.create.repository.CreatePaymentDataRepository
import io.primer.android.data.payments.resume.datasource.ResumePaymentDataSource
import io.primer.android.data.payments.resume.repository.ResumePaymentDataRepository
import io.primer.android.data.tokenization.helper.PrimerPaymentMethodDataHelper
import io.primer.android.domain.payments.create.CreatePaymentInteractor
import io.primer.android.domain.payments.create.repository.CreatePaymentRepository
import io.primer.android.domain.payments.helpers.PaymentResultEventsResolver
import io.primer.android.domain.payments.resume.ResumePaymentInteractor
import io.primer.android.domain.payments.resume.respository.ResumePaymentsRepository
import io.primer.android.viewmodel.PrimerViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class PaymentsContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { CreatePaymentDataSource(sdk.resolve()) }

        registerSingleton { ResumePaymentDataSource(sdk.resolve()) }

        registerSingleton {
            PrimerPaymentMethodDataHelper(
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton<CreatePaymentRepository> {
            CreatePaymentDataRepository(resolve(), sdk.resolve(), sdk.resolve(), resolve())
        }

        registerSingleton<ResumePaymentsRepository> {
            ResumePaymentDataRepository(
                resolve(),
                sdk.resolve(),
                resolve()
            )
        }

        registerFactory {
            PaymentResultEventsResolver(
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerFactory {
            ResumePaymentInteractor(
                resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerFactory {
            CreatePaymentInteractor(
                resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerFactory {
            PrimerViewModelFactory(
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }
    }
}
