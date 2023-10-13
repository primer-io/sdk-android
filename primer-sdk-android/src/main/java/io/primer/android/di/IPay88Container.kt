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
import io.primer.android.di.ResumeEventContainer.Companion.RESUME_HANDLER_LOGGER_NAME
import io.primer.android.payment.async.ipay88.resume.IPay88ResumeDecisionHandler

internal class IPay88Container(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory { ValidClientSessionAmountRule() }

        registerFactory { ValidClientSessionCurrencyRule() }

        registerFactory { ValidClientSessionCountryCodeRule() }

        registerFactory { ValidProductDescriptionRule() }

        registerFactory { ValidCustomerFirstNameRule() }

        registerFactory { ValidCustomerLastNameRule() }

        registerFactory { ValidCustomerEmailRule() }

        registerFactory { ValidRemarkRule() }

        registerFactory {
            IPay88ValidationRulesResolver(
                resolve(),
                resolve(),
                resolve(),
                resolve(),
                resolve(),
                resolve(),
                resolve(),
                resolve()
            )
        }

        registerFactory<PrimerResumeDecisionHandler> {
            IPay88ResumeDecisionHandler(
                sdk.resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(RESUME_HANDLER_LOGGER_NAME),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }
    }
}
