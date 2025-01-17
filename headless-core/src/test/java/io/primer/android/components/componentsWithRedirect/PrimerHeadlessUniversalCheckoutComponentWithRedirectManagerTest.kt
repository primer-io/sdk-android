package io.primer.android.components.componentsWithRedirect

import android.content.Context
import androidx.lifecycle.ViewModelStoreOwner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.primer.android.PrimerSessionIntent
import io.primer.android.banks.di.BanksComponentProvider
import io.primer.android.components.PaymentMethodManagerDelegate
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.componentWithRedirect.PrimerHeadlessUniversalCheckoutComponentWithRedirectManager
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.manager.componentWithRedirect.component.PrimerHeadlessMainComponent
import io.primer.android.components.manager.redirect.di.WebRedirectComponentProvider
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.ConcurrentHashMap

class PrimerHeadlessUniversalCheckoutComponentWithRedirectManagerTest {
    private lateinit var viewModelStoreOwner: ViewModelStoreOwner
    private lateinit var paymentMethodManagerDelegate: PaymentMethodManagerDelegate
    private lateinit var context: Context

    @BeforeEach
    fun setUp() {
        viewModelStoreOwner = mockk()
        paymentMethodManagerDelegate = mockk(relaxed = true)
        context = mockk()

        mockkObject(BanksComponentProvider)
        mockkObject(WebRedirectComponentProvider)

        DISdkContext.clear()
        DISdkContext.headlessSdkContainer =
            mockk<SdkContainer>(relaxed = true).also { sdkContainer ->
                val cont =
                    spyk<DependencyContainer>().also { container ->
                        container.registerFactory<PaymentMethodManagerDelegate> { paymentMethodManagerDelegate }
                        container.registerFactory<Context> { context }
                    }

                every { sdkContainer.containers }
                    .returns(ConcurrentHashMap(mutableMapOf(cont::class.simpleName.orEmpty() to cont)))
            }
    }

    @Test
    fun `provide should initialize and start payment method`() =
        runTest {
            val paymentMethodType = PaymentMethodType.ADYEN_IDEAL.name
            val category = PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT

            val component = PrimerHeadlessUniversalCheckoutComponentWithRedirectManager(viewModelStoreOwner)

            every { BanksComponentProvider.provideInstance(any(), any(), any()) } returns mockk(relaxed = true)

            component.provide<PrimerHeadlessMainComponent<PrimerCollectableData, PrimerHeadlessStep>>(paymentMethodType)

            coVerify {
                paymentMethodManagerDelegate.init(paymentMethodType, category)
                paymentMethodManagerDelegate.start(
                    context = context,
                    paymentMethodType = paymentMethodType,
                    sessionIntent = PrimerSessionIntent.CHECKOUT,
                    category = category,
                    onPostStart = any(),
                )
                BanksComponentProvider.provideInstance(any(), any(), any())
            }
        }

    @Test
    fun `provide should throw UnsupportedPaymentMethodException for unsupported payment methods`() {
        val paymentMethodType = "UNSUPPORTED_PAYMENT_METHOD"
        val component = PrimerHeadlessUniversalCheckoutComponentWithRedirectManager(viewModelStoreOwner)

        assertThrows<UnsupportedPaymentMethodException> {
            runTest {
                component.provide<PrimerHeadlessMainComponent<PrimerCollectableData, PrimerHeadlessStep>>(
                    paymentMethodType,
                )
            }
        }
    }

    @Test
    fun `provide should handle ClassCastException and throw UnsupportedPaymentMethodException`() {
        val paymentMethodType = PaymentMethodType.ADYEN_IDEAL.name

        val component = PrimerHeadlessUniversalCheckoutComponentWithRedirectManager(viewModelStoreOwner)

        every { BanksComponentProvider.provideInstance(any(), any(), any()) } throws ClassCastException()

        assertThrows<UnsupportedPaymentMethodException> {
            runTest {
                component.provide<PrimerHeadlessMainComponent<PrimerCollectableData, PrimerHeadlessStep>>(
                    paymentMethodType,
                )
            }
        }
    }

    @Test
    fun `provide should throw SdkUninitializedException when SDK is not initialized`() {
        val paymentMethodType = PaymentMethodType.ADYEN_IDEAL.name
        val component = PrimerHeadlessUniversalCheckoutComponentWithRedirectManager(viewModelStoreOwner)

        coEvery { paymentMethodManagerDelegate.init(any(), any()) } throws SdkUninitializedException()

        assertThrows<SdkUninitializedException> {
            runTest {
                component.provide<PrimerHeadlessMainComponent<PrimerCollectableData, PrimerHeadlessStep>>(
                    paymentMethodType,
                )
            }
        }
    }
}
