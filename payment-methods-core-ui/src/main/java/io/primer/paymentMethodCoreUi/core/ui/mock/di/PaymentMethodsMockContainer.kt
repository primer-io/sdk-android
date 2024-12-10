package io.primer.paymentMethodCoreUi.core.ui.mock.di

import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.paymentMethodCoreUi.core.ui.mock.PaymentMethodMockViewModelFactory

class PaymentMethodsMockContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory {
            PaymentMethodMockViewModelFactory(
                finaliseMockedFlowInteractor = sdk().resolve(),
                paymentResumeHandler = sdk().resolve(),
                errorHandler = sdk().resolve(),
                successHandler = sdk().resolve()
            )
        }
    }
}
