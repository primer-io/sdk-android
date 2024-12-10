package io.primer.android.googlepay

import android.content.Context
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.primer.android.core.logging.internal.LogReporter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DefaultGooglePayFacadeFactoryTest {

    @MockK
    private lateinit var applicationContext: Context

    @MockK
    private lateinit var logReporter: LogReporter

    private lateinit var factory: DefaultGooglePayFacadeFactory

    @BeforeEach
    fun setUp() {
        factory = DefaultGooglePayFacadeFactory()
        mockkStatic(Wallet::class)
    }

    @AfterEach
    fun tearDown() {
        mockkStatic(Wallet::class)
    }

    @Test
    fun `create should return GooglePayFacade with test environment when the environment is set to ENVIRONMENT_TEST`() {
        // Arrange
        val mockPaymentsClient = mockk<PaymentsClient>(relaxed = true)
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build()

        every { Wallet.getPaymentsClient(applicationContext, walletOptions) } returns mockPaymentsClient

        // Act
        val googlePayFacade = factory.create(
            applicationContext,
            GooglePayFacade.Environment.TEST,
            logReporter
        )

        // Assert
        assertNotNull(googlePayFacade)
        verify { Wallet.getPaymentsClient(applicationContext, walletOptions) }
    }

    @Test
    fun `create should return GooglePayFacade with production environment, when the environment is set to ENVIRONMENT_PRODUCTION`() {
        // Arrange
        val mockPaymentsClient = mockk<PaymentsClient>(relaxed = true)
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
            .build()

        every { Wallet.getPaymentsClient(applicationContext, walletOptions) } returns mockPaymentsClient

        // Act
        val googlePayFacade = factory.create(
            applicationContext,
            GooglePayFacade.Environment.PRODUCTION,
            logReporter
        )

        // Assert
        assertNotNull(googlePayFacade)
        verify { Wallet.getPaymentsClient(applicationContext, walletOptions) }
    }
}
