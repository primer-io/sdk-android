package io.primer.android.components.componentWithRedirect

import android.content.Context
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.PrimerSessionIntent
import io.primer.android.banks.di.BanksComponentProvider
import io.primer.android.components.PaymentMethodManagerDelegate
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.manager.componentWithRedirect.component.PrimerHeadlessMainComponent
import io.primer.android.components.manager.redirect.di.WebRedirectComponentProvider
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.core.di.extensions.resolve
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep

class PrimerHeadlessUniversalCheckoutComponentWithRedirectManager(
    private val viewModelStoreOwner: ViewModelStoreOwner
) : DISdkComponent {

    private val paymentMethodInitializer: PaymentMethodManagerDelegate by inject()

    @Throws(SdkUninitializedException::class, UnsupportedPaymentMethodException::class)
    fun <T : PrimerHeadlessMainComponent<
            out PrimerCollectableData,
            out PrimerHeadlessStep>> provide(
        paymentMethodType: String
    ): T {
        val category: PrimerPaymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT

        paymentMethodInitializer.apply {
            init(
                paymentMethodType = paymentMethodType,
                category = category
            ).also {
                start(
                    context = resolve<Context>(),
                    paymentMethodType = paymentMethodType,
                    sessionIntent = PrimerSessionIntent.CHECKOUT,
                    category = category,
                    onPostStart = {}
                )
            }
        }

        @Suppress("UNCHECKED_CAST", "detekt:SwallowedException")
        return try {
            when (paymentMethodType) {
                PaymentMethodType.ADYEN_IDEAL.name, PaymentMethodType.ADYEN_DOTPAY.name -> {
                    BanksComponentProvider.provideInstance(
                        owner = viewModelStoreOwner,
                        paymentMethodType = paymentMethodType,
                        onFinished = {
                            WebRedirectComponentProvider.provideInstance(
                                owner = viewModelStoreOwner,
                                paymentMethodType = paymentMethodType
                            ).start()
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
