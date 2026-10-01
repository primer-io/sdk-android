package io.primer.checkout.orchestrator

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BdcClientRequirementsNotMetContextParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.checkout.orchestrator.domain.model.ClientRequirementsCheck
import io.primer.checkout.orchestrator.domain.repository.ClientRequirementsRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class BackendDrivenPaymentMethodCheckerTest {

    @MockK
    lateinit var clientRequirementsRepository: ClientRequirementsRepository

    @MockK
    lateinit var settings: PrimerSettings

    @MockK
    lateinit var analyticsInteractor: AnalyticsInteractor

    private val logReporter = mockk<LogReporter>(relaxed = true)
    private val paymentMethod = mockk<PaymentMethod> { every { type } returns TYPE }
    private lateinit var checker: BackendDrivenPaymentMethodChecker

    @BeforeEach
    fun setUp() {
        every { settings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
        coEvery { analyticsInteractor(any()) } returns Result.success(Unit)
        checker = BackendDrivenPaymentMethodChecker(
            clientRequirementsRepository = clientRequirementsRepository,
            settings = settings,
            analyticsInteractor = analyticsInteractor,
            logReporter = logReporter,
        )
    }

    @Test
    fun `is available when the state processor finds its requirements met`() = runTest {
        givenVerdicts(mapOf(TYPE to ClientRequirementsCheck(satisfied = true, unmet = "[]")))

        assertTrue(checker.shouldPaymentMethodBeAvailable(paymentMethod))
    }

    @Test
    fun `hides the method, warns and reports when a requirement is unmet`() = runTest {
        givenVerdicts(mapOf(TYPE to ClientRequirementsCheck(satisfied = false, unmet = UNMET)))

        assertFalse(checker.shouldPaymentMethodBeAvailable(paymentMethod))
        verify {
            logReporter.warn(
                match { it.contains("was filtered out") && it.contains("https://primer.io/docs/checkout/drop-in") },
            )
        }
        coVerify {
            analyticsInteractor(
                match<MessageAnalyticsParams> {
                    it.messageType == MessageType.BDC_CLIENT_REQUIREMENTS_NOT_MET &&
                        it.context == BdcClientRequirementsNotMetContextParams(paymentMethodType = TYPE, unmet = UNMET)
                },
            )
        }
    }

    @Test
    fun `points a headless integration to the headless guide`() = runTest {
        every { settings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        givenVerdicts(mapOf(TYPE to ClientRequirementsCheck(satisfied = false, unmet = UNMET)))

        checker.shouldPaymentMethodBeAvailable(paymentMethod)

        verify { logReporter.warn(match { it.contains("https://primer.io/docs/checkout/headless") }) }
    }

    @Test
    fun `hides the method when the requirements could not be checked`() = runTest {
        coEvery { clientRequirementsRepository.getVerdicts() } returns Result.failure(IllegalStateException("boom"))

        assertFalse(checker.shouldPaymentMethodBeAvailable(paymentMethod))
        verify { logReporter.warn(match { it.contains("could not be checked") && it.contains("boom") }) }
    }

    @Test
    fun `hides a method the verdicts do not cover`() = runTest {
        givenVerdicts(emptyMap())

        assertFalse(checker.shouldPaymentMethodBeAvailable(paymentMethod))
        verify { logReporter.warn(match { it.contains("is not in the checked configuration") }) }
    }

    private fun givenVerdicts(verdicts: Map<String, ClientRequirementsCheck>) {
        coEvery { clientRequirementsRepository.getVerdicts() } returns Result.success(verdicts)
    }

    private companion object {
        const val TYPE = "TWINT"
        const val UNMET = """[{"kind":"check","name":"returnUrl","reason":"unsatisfied"}]"""
    }
}
