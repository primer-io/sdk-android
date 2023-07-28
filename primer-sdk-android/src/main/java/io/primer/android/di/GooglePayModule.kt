package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.repository.GooglePayConfigurationDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.GooglePayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.repository.GooglePayConfigurationRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.validation.GooglePayValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.validation.GooglePayValidPaymentDataMethodRule
import org.koin.dsl.module

internal val googlePayModule = {
    module {
        single<GooglePayConfigurationRepository> {
            GooglePayConfigurationDataRepository(
                get(),
                get()
            )
        }
        single {
            GooglePayConfigurationInteractor(
                get(),
                get()
            )
        }
        single { GooglePayValidPaymentDataMethodRule() }
        single {
            GooglePayValidationRulesResolver(
                get()
            )
        }
    }
}
