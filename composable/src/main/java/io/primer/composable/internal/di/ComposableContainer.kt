package io.primer.composable.internal.di

import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManager
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.composable.internal.data.mappers.PaymentMethodMapper
import io.primer.composable.internal.data.mappers.PaymentMethodMapperImpl
import io.primer.composable.internal.data.repositories.HeadlessRepositoryImpl
import io.primer.composable.internal.data.repositories.RawDataManagerRepositoryImpl
import io.primer.composable.internal.domain.interactor.GetAvailablePaymentMethodsInteractor
import io.primer.composable.internal.domain.interactor.GetRequiredFieldsInteractor
import io.primer.composable.internal.domain.interactor.GetValidationStateInteractor
import io.primer.composable.internal.domain.interactor.SetCardDataInteractor
import io.primer.composable.internal.domain.interactor.SubmitPaymentInteractor
import io.primer.composable.internal.domain.interactor.TrackDirtyFieldsInteractor
import io.primer.composable.internal.domain.interactor.ValidateBillingAddressInteractor
import io.primer.composable.internal.domain.repositories.HeadlessRepository
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository
import io.primer.composable.internal.presentation.checkout.CheckoutNavigator
import io.primer.ui.core.configuration.domain.model.BasicOrderInfoInteractor
import io.primer.ui.core.payment.domain.interactor.SurchargeInteractor

internal class ComposableContainer(
    @Suppress(
        "UNUSED_PARAMETER",
    ) private val sdk: () -> SdkContainer,
) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<PaymentMethodMapper> {
            PaymentMethodMapperImpl()
        }

        registerSingleton<HeadlessRepository> {
            HeadlessRepositoryImpl(PrimerHeadlessUniversalCheckout.current)
        }

        registerSingleton {
            SurchargeInteractor(sdk().resolve())
        }

        registerSingleton {
            BasicOrderInfoInteractor(sdk().resolve())
        }

        registerSingleton<RawDataManagerRepository> {
            RawDataManagerRepositoryImpl(
                PrimerHeadlessUniversalCheckoutRawDataManager.newInstance(
                    PaymentMethodType.PAYMENT_CARD.name,
                ),
            )
        }

        registerSingleton {
            GetAvailablePaymentMethodsInteractor()
        }

        registerSingleton {
            GetRequiredFieldsInteractor()
        }

        registerSingleton {
            TrackDirtyFieldsInteractor()
        }

        registerSingleton {
            SetCardDataInteractor()
        }

        registerSingleton {
            GetValidationStateInteractor()
        }

        registerSingleton {
            ValidateBillingAddressInteractor()
        }

        registerSingleton {
            SubmitPaymentInteractor()
        }

        registerSingleton {
            CheckoutNavigator()
        }
    }
}
