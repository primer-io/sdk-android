package io.primer.android.payments.di

import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.payments.core.create.data.datasource.CreatePaymentDataSource
import io.primer.android.payments.core.create.data.datasource.LocalPaymentDataSource
import io.primer.android.payments.core.errors.data.mapper.PaymentCreateErrorMapper
import io.primer.android.payments.core.create.data.repository.CreatePaymentDataRepository
import io.primer.android.payments.core.create.data.repository.PaymentResultDataRepository
import io.primer.android.payments.core.create.domain.CreatePaymentInteractor
import io.primer.android.payments.core.create.domain.DefaultCreatePaymentInteractor
import io.primer.android.payments.core.create.domain.handler.DefaultPaymentMethodTokenHandler
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.create.domain.repository.CreatePaymentRepository
import io.primer.android.payments.core.create.domain.repository.PaymentResultRepository
import io.primer.android.payments.core.errors.data.mapper.PaymentResumeErrorMapper
import io.primer.android.payments.core.helpers.PaymentDecisionResolver
import io.primer.android.payments.core.resume.data.datasource.ResumePaymentDataSource
import io.primer.android.payments.core.resume.data.repository.ResumePaymentDataRepository
import io.primer.android.payments.core.resume.domain.DefaultResumePaymentInteractor
import io.primer.android.payments.core.resume.domain.ResumePaymentInteractor
import io.primer.android.payments.core.resume.domain.handler.DefaultPaymentResumeHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.resume.domain.respository.ResumePaymentsRepository
import io.primer.android.payments.core.status.data.datasource.RemoteAsyncPaymentMethodStatusDataSource
import io.primer.android.payments.core.status.data.repository.AsyncPaymentMethodStatusDataRepository
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.DefaultAsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.repository.AsyncPaymentMethodStatusRepository
import io.primer.android.payments.core.tokenization.data.repository.TokenizedPaymentMethodDataRepository
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

class PaymentsContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<TokenizedPaymentMethodRepository> { TokenizedPaymentMethodDataRepository() }

        registerSingleton {
            RemoteAsyncPaymentMethodStatusDataSource(
                primerHttpClient = sdk().resolve()
            )
        }
        registerSingleton<AsyncPaymentMethodStatusRepository> {
            AsyncPaymentMethodStatusDataRepository(
                asyncPaymentMethodStatusDataSource = resolve()
            )
        }

        registerSingleton { LocalPaymentDataSource() }

        registerSingleton<PaymentResultRepository> {
            PaymentResultDataRepository(
                localPaymentDataSource = resolve()
            )
        }

        registerSingleton { CreatePaymentDataSource(primerHttpClient = sdk().resolve()) }

        registerSingleton { ResumePaymentDataSource(primerHttpClient = sdk().resolve()) }

        registerSingleton<CreatePaymentRepository> {
            CreatePaymentDataRepository(
                createPaymentDataSource = resolve(),
                localPaymentDataSource = sdk().resolve(),
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY)
            )
        }

        registerSingleton<ResumePaymentsRepository> {
            ResumePaymentDataRepository(
                resumePaymentDataSource = resolve(),
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY)
            )
        }

        registerFactory {
            PaymentDecisionResolver(
                tokenizedPaymentMethodRepository = sdk().resolve(),
                logReporter = sdk().resolve()
            )
        }

        registerFactory<ResumePaymentInteractor>(name = RESUME_PAYMENT_INTERACTOR_DI_KEY) {
            DefaultResumePaymentInteractor(
                resumePaymentsRepository = resolve(),
                paymentDecisionResolver = resolve(),
                logReporter = sdk().resolve()
            )
        }

        registerFactory<CreatePaymentInteractor>(name = CREATE_PAYMENT_INTERACTOR_DI_KEY) {
            DefaultCreatePaymentInteractor(
                createPaymentsRepository = resolve(),
                paymentDecisionResolver = resolve(),
                logReporter = sdk().resolve()
            )
        }

        registerFactory<PaymentMethodTokenHandler> {
            DefaultPaymentMethodTokenHandler(config = sdk().resolve())
        }

        registerFactory<PaymentResumeHandler> {
            DefaultPaymentResumeHandler(config = sdk().resolve())
        }

        registerFactory<AsyncPaymentMethodPollingInteractor>(name = POLLING_INTERACTOR_DI_KEY) {
            DefaultAsyncPaymentMethodPollingInteractor(
                paymentMethodStatusRepository = resolve()
            )
        }

        sdk().resolve<ErrorMapperRegistry>().register(PaymentCreateErrorMapper())
        sdk().resolve<ErrorMapperRegistry>().register(PaymentResumeErrorMapper())
    }

    companion object {

        const val CREATE_PAYMENT_INTERACTOR_DI_KEY = "CREATE_PAYMENT_INTERACTOR"
        const val RESUME_PAYMENT_INTERACTOR_DI_KEY = "RESUME_PAYMENT_INTERACTOR"
        const val POLLING_INTERACTOR_DI_KEY = "POLLING_INTERACTOR"
    }
}
