package io.primer.android.di

import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.resolvers.IPay88ValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidClientSessionAmountRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidClientSessionCountryCodeRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidClientSessionCurrencyRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidCustomerEmailRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidCustomerFirstNameRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidCustomerLastNameRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidProductDescriptionRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidRemarkRule
import io.primer.android.payment.async.ipay88.resume.IPay88ResumeDecisionHandler
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val iPay88Module = {
    module {

        factory { ValidClientSessionAmountRule() }
        factory { ValidClientSessionCurrencyRule() }
        factory { ValidClientSessionCountryCodeRule() }
        factory { ValidProductDescriptionRule() }
        factory { ValidCustomerFirstNameRule() }
        factory { ValidCustomerLastNameRule() }
        factory { ValidCustomerEmailRule() }
        factory { ValidRemarkRule() }

        factory {
            IPay88ValidationRulesResolver(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }

        factory<PrimerResumeDecisionHandler> {
            IPay88ResumeDecisionHandler(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(named(RESUME_HANDLER_LOGGER_NAME)),
                get(),
                get(),
                get()
            )
        }
    }
}
