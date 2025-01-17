package io.primer.android.payments.core.create.domain.handler

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.payments.core.create.domain.CreatePaymentInteractor
import io.primer.android.payments.core.create.domain.model.CreatePaymentParams
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.di.PaymentsContainer
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals

class DefaultPaymentMethodTokenHandlerTest {
    private lateinit var config: PrimerConfig
    private lateinit var createPaymentInteractor: CreatePaymentInteractor
    private lateinit var postTokenizationHandler: PostTokenizationHandler
    private lateinit var handler: DefaultPaymentMethodTokenHandler

    @BeforeEach
    fun setUp() {
        config = mockk()
        createPaymentInteractor = mockk(relaxed = true)
        postTokenizationHandler = mockk(relaxed = true)

        DISdkContext.headlessSdkContainer =
            mockk<SdkContainer>(relaxed = true).also { sdkContainer ->
                val cont =
                    spyk<DependencyContainer>().also { container ->
                        container.registerFactory<CreatePaymentInteractor>(
                            name = PaymentsContainer.CREATE_PAYMENT_INTERACTOR_DI_KEY,
                        ) { createPaymentInteractor }
                        container.registerFactory<CreatePaymentInteractor> { createPaymentInteractor }
                        container.registerFactory<PostTokenizationHandler> { postTokenizationHandler }
                    }

                every { sdkContainer.containers }
                    .returns(ConcurrentHashMap(mutableMapOf(cont::class.simpleName.orEmpty() to cont)))
            }

        handler = DefaultPaymentMethodTokenHandler(config)
    }

    @Test
    fun `handle should use AUTO strategy by default`() =
        runTest {
            // Arrange
            val paymentMethodTokenData =
                mockk<PrimerPaymentMethodTokenData>(relaxed = true) {
                    every { token } returns "testToken"
                }
            val primerSessionIntent = PrimerSessionIntent.CHECKOUT
            val paymentDecision = mockk<PaymentDecision>()

            every { config.settings.paymentHandling } returns PrimerPaymentHandling.AUTO
            coEvery { createPaymentInteractor.invoke(CreatePaymentParams("testToken")) } returns
                Result.success(
                    paymentDecision,
                )

            // Act
            val result = handler.handle(paymentMethodTokenData, primerSessionIntent)

            // Assert
            assertEquals(Result.success(paymentDecision), result)
            coVerify { createPaymentInteractor.invoke(CreatePaymentParams("testToken")) }
        }

    @Test
    fun `handle should use MANUAL strategy when configured`() =
        runTest {
            // Arrange
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
            val primerSessionIntent = PrimerSessionIntent.CHECKOUT
            val paymentDecision = mockk<PaymentDecision>()

            every { config.settings.paymentHandling } returns PrimerPaymentHandling.MANUAL
            coEvery { postTokenizationHandler.handle(paymentMethodTokenData) } returns Result.success(paymentDecision)

            // Act
            val result = handler.handle(paymentMethodTokenData, primerSessionIntent)

            // Assert
            assertEquals(Result.success(paymentDecision), result)
            coVerify { postTokenizationHandler.handle(paymentMethodTokenData) }
        }

    @Test
    fun `handle should use VAULT strategy when intent is VAULT`() =
        runTest {
            // Arrange
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
            val primerSessionIntent = PrimerSessionIntent.VAULT
            val paymentDecision = mockk<PaymentDecision>()

            every { config.settings.paymentHandling } returns PrimerPaymentHandling.AUTO
            coEvery { postTokenizationHandler.handle(paymentMethodTokenData) } returns Result.success(paymentDecision)

            // Act
            val result = handler.handle(paymentMethodTokenData, primerSessionIntent)

            // Assert
            assertEquals(Result.success(paymentDecision), result)
            coVerify { postTokenizationHandler.handle(paymentMethodTokenData) }
        }
}
