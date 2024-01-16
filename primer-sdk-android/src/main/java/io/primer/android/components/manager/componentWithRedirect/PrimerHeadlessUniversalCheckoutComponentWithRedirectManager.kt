package io.primer.android.components.manager.componentWithRedirect

import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.manager.banks.component.DefaultBanksComponent
import io.primer.android.components.manager.componentWithRedirect.component.PrimerHeadlessMainComponent
import io.primer.android.components.manager.core.composable.PrimerCollectableData
import io.primer.android.components.manager.core.composable.PrimerHeadlessStep
import io.primer.android.components.manager.redirect.component.WebRedirectComponent
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.di.DISdkComponent
import io.primer.android.di.RpcContainer
import io.primer.android.di.extension.inject
import io.primer.android.domain.exception.UnsupportedPaymentMethodException

class PrimerHeadlessUniversalCheckoutComponentWithRedirectManager(
    private val viewModelStoreOwner: ViewModelStoreOwner
) : DISdkComponent {
    private val headlessManagerDelegate: DefaultHeadlessManagerDelegate by inject()

    @Throws(SdkUninitializedException::class, UnsupportedPaymentMethodException::class)
    fun <T : PrimerHeadlessMainComponent<
            out PrimerCollectableData,
            out PrimerHeadlessStep>> provide(
        paymentMethodType: String
    ): T {
        val rpcContainer = RpcContainer(getSdkContainer())
        getSdkContainer().registerContainer(rpcContainer)

        headlessManagerDelegate.init(
            paymentMethodType = paymentMethodType,
            category = PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT
        )
        @Suppress("UNCHECKED_CAST", "detekt:SwallowedException")
        return try {
            when (paymentMethodType) {
                PaymentMethodType.ADYEN_IDEAL.name, PaymentMethodType.ADYEN_DOTPAY.name -> {
                    DefaultBanksComponent.provideInstance(
                        owner = viewModelStoreOwner,
                        paymentMethodType = paymentMethodType,
                        onFinished = {
                            WebRedirectComponent.provideInstance(
                                owner = viewModelStoreOwner,
                                paymentMethodType = paymentMethodType
                            ).start()
                        },
                        onDisposed = {
                            getSdkContainer().unregisterContainer<RpcContainer>()
                        }
                    )
                }

                else -> throw UnsupportedPaymentMethodException(paymentMethodType)
            } as T
        } catch (e: ClassCastException) {
            throw UnsupportedPaymentMethodException(paymentMethodType)
        }
    }
}
