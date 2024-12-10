package io.primer.android.klarna.implementation.session.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.PrimerSessionIntent
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.ClientSessionData
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.domain.action.models.PrimerFee
import io.primer.android.klarna.implementation.session.domain.KlarnaSessionInteractor
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSession
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSessionParams
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class KlarnaSessionCreationDelegateTest {
    @MockK
    private lateinit var primerSessionIntent: PrimerSessionIntent

    @MockK
    private lateinit var actionInteractor: ActionInteractor

    @MockK
    private lateinit var klarnaSessionInteractor: KlarnaSessionInteractor

    @MockK
    private lateinit var primerSettings: PrimerSettings

    @MockK
    private lateinit var configurationInteractor: ConfigurationInteractor

    private lateinit var delegate: KlarnaSessionCreationDelegate

    @BeforeEach
    fun setUp() {
        delegate = KlarnaSessionCreationDelegate(
            actionInteractor = actionInteractor,
            interactor = klarnaSessionInteractor,
            primerSettings = primerSettings,
            configurationInteractor = configurationInteractor
        )
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(klarnaSessionInteractor)
    }

    @Test
    fun `createSession() should return session emitted by session interactor when called and SURCHARGE fee exists and integration type is HEADLESS`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        coEvery { actionInteractor.invoke(any<MultipleActionUpdateParams>()) } returns Result.success(
            mockk<ClientSessionData> {
                every { clientSession.fees } returns listOf(
                    PrimerFee(type = "SURCHARGE", amount = 140)
                )
            }
        )
        val klarnaSession = mockk<KlarnaSession>()
        coEvery { klarnaSessionInteractor.invoke(any()) } returns Result.success(klarnaSession)

        val result = delegate.createSession(primerSessionIntent).getOrNull()

        assertEquals(klarnaSession, result)
        coVerify(exactly = 1) {
            actionInteractor.invoke(
                MultipleActionUpdateParams(
                    params = listOf(
                        ActionUpdateSelectPaymentMethodParams(
                            paymentMethodType = PaymentMethodType.KLARNA.name,
                            cardNetwork = null
                        )
                    )
                )
            )
            klarnaSessionInteractor.invoke(KlarnaSessionParams(140, primerSessionIntent))
        }
    }

    @Test
    fun `createSession() should return session emitted by session interactor when called and SURCHARGE fee exists and integration type is DROP_IN`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
        coEvery { configurationInteractor.invoke(any()) } returns Result.success(
            mockk<Configuration> {
                every { clientSession.clientSessionDataResponse.order?.toFees() } returns listOf(
                    PrimerFee(type = "SURCHARGE", amount = 140)
                )
            }
        )
        val klarnaSession = mockk<KlarnaSession>()
        coEvery { klarnaSessionInteractor.invoke(any()) } returns Result.success(klarnaSession)

        val result = delegate.createSession(primerSessionIntent).getOrNull()

        assertEquals(klarnaSession, result)
        coVerify(exactly = 1) {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
            klarnaSessionInteractor.invoke(KlarnaSessionParams(140, primerSessionIntent))
        }
    }

    @Test
    fun `createSession() should return session emitted by session interactor when called and SURCHARGE fee doesn't exist and integration type is HEADLESS`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        coEvery { actionInteractor.invoke(any<MultipleActionUpdateParams>()) } returns Result.success(
            mockk<ClientSessionData> {
                every { clientSession.fees } returns listOf(
                    PrimerFee(type = "not a surcharge", amount = 140)
                )
            }
        )
        val klarnaSession = mockk<KlarnaSession>()
        coEvery { klarnaSessionInteractor.invoke(any()) } returns Result.success(klarnaSession)

        val result = delegate.createSession(primerSessionIntent).getOrNull()

        assertEquals(klarnaSession, result)
        coVerify(exactly = 1) {
            actionInteractor.invoke(
                MultipleActionUpdateParams(
                    params = listOf(
                        ActionUpdateSelectPaymentMethodParams(
                            paymentMethodType = PaymentMethodType.KLARNA.name,
                            cardNetwork = null
                        )
                    )
                )
            )
            klarnaSessionInteractor.invoke(KlarnaSessionParams(null, primerSessionIntent))
        }
    }

    @Test
    fun `createSession() should return session emitted by session interactor when called and SURCHARGE fee doesn't exist and integration type is DROP_IN`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
        coEvery { configurationInteractor.invoke(any()) } returns Result.success(
            mockk<Configuration> {
                every { clientSession.clientSessionDataResponse.order?.toFees() } returns listOf(
                    PrimerFee(type = "not a surcharge", amount = 140)
                )
            }
        )
        val klarnaSession = mockk<KlarnaSession>()
        coEvery { klarnaSessionInteractor.invoke(any()) } returns Result.success(klarnaSession)

        val result = delegate.createSession(primerSessionIntent).getOrNull()

        assertEquals(klarnaSession, result)
        coVerify(exactly = 1) {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
            klarnaSessionInteractor.invoke(KlarnaSessionParams(null, primerSessionIntent))
        }
    }

    @Test
    fun `createSession() should return error emitted by session interactor when called and integration type is HEADLESS`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        coEvery { actionInteractor.invoke(any<MultipleActionUpdateParams>()) } returns Result.success(
            mockk<ClientSessionData> {
                every { clientSession.fees } returns emptyList()
            }
        )
        val exception = Exception()
        coEvery { klarnaSessionInteractor.invoke(any()) } returns Result.failure(exception)

        val result = delegate.createSession(primerSessionIntent).exceptionOrNull()

        assertEquals(exception, result)
        coVerify(exactly = 1) {
            actionInteractor.invoke(
                MultipleActionUpdateParams(
                    params = listOf(
                        ActionUpdateSelectPaymentMethodParams(
                            paymentMethodType = PaymentMethodType.KLARNA.name,
                            cardNetwork = null
                        )
                    )
                )
            )
            klarnaSessionInteractor.invoke(any())
        }
    }

    @Test
    fun `createSession() should return error emitted by session interactor when called and integration type is DROP_IN`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
        coEvery { configurationInteractor.invoke(any()) } returns Result.success(
            mockk<Configuration> {
                every { clientSession.clientSessionDataResponse.order?.toFees() } returns listOf()
            }
        )
        val exception = Exception()
        coEvery { klarnaSessionInteractor.invoke(any()) } returns Result.failure(exception)

        val result = delegate.createSession(primerSessionIntent).exceptionOrNull()

        assertEquals(exception, result)
        coVerify(exactly = 1) {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
            klarnaSessionInteractor.invoke(any())
        }
    }

    @Test
    fun `createSession() should return error emitted by action interactor when called and integration type is HEADLESS`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        val exception = Exception()
        coEvery { actionInteractor.invoke(any<MultipleActionUpdateParams>()) } returns Result.failure(exception)
        coEvery { klarnaSessionInteractor.invoke(any()) } returns Result.success(mockk())

        val result = delegate.createSession(primerSessionIntent).exceptionOrNull()

        assertEquals(exception, result)
        coVerify(exactly = 1) {
            actionInteractor.invoke(
                MultipleActionUpdateParams(
                    params = listOf(
                        ActionUpdateSelectPaymentMethodParams(
                            paymentMethodType = PaymentMethodType.KLARNA.name,
                            cardNetwork = null
                        )
                    )
                )
            )
        }
        coVerify(exactly = 0) {
            klarnaSessionInteractor.invoke(any())
        }
    }

    @Test
    fun `createSession() should return error emitted by action interactor when called and integration type is DROP_IN`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
        val exception = Exception()
        coEvery { configurationInteractor.invoke(any()) } returns Result.failure(exception)
        coEvery { klarnaSessionInteractor.invoke(any()) } returns Result.success(mockk())

        val result = delegate.createSession(primerSessionIntent).exceptionOrNull()

        assertEquals(exception, result)
        coVerify(exactly = 1) {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify(exactly = 0) {
            klarnaSessionInteractor.invoke(any())
        }
    }
}
