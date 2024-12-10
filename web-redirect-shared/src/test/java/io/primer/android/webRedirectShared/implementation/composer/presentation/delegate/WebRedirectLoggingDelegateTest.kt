package io.primer.android.webRedirectShared.implementation.composer.presentation.delegate

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.components.manager.redirect.composable.WebRedirectStep
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class WebRedirectLoggingDelegateTest {
    private val paymentMethodType = "paymentMethodType"

    @MockK
    private lateinit var logReporter: LogReporter

    @MockK
    private lateinit var analyticsInteractor: AnalyticsInteractor

    private lateinit var delegate: WebRedirectLoggingDelegate

    @BeforeEach
    fun setUp() {
        delegate =
            WebRedirectLoggingDelegate(
                logReporter = logReporter,
                analyticsInteractor = analyticsInteractor
            )
    }

    @Test
    fun `logError() logs error via log reporter and analytics interactor when called`() = runTest {
        val error = mockk<PrimerError> {
            every { errorId } returns "errorId"
            every { description } returns "description"
            every { diagnosticsId } returns "diagnosticsId"
        }
        coEvery { analyticsInteractor(any()) } returns Result.success(Unit)
        every { logReporter.error(any()) } just Runs

        delegate.logError(error = error, paymentMethodType = paymentMethodType)

        coVerify(exactly = 1) {
            analyticsInteractor(
                MessageAnalyticsParams(
                    messageType = MessageType.ERROR,
                    message = "$paymentMethodType: description",
                    severity = Severity.ERROR,
                    diagnosticsId = "diagnosticsId",
                    context = ErrorContextParams(
                        errorId = "errorId",
                        paymentMethodType = paymentMethodType
                    )
                )
            )
            logReporter.error("description")
        }
    }

    @ParameterizedTest
    @MethodSource("provideSteps")
    fun `logStep() logs steps via log reporter and analytics interactor when called`(
        step: WebRedirectStep
    ) = runTest {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid"
        coEvery { analyticsInteractor(any()) } returns Result.success(Unit)
        every { logReporter.info(any()) } just Runs

        delegate.logStep(webRedirectStep = step, paymentMethodType = paymentMethodType)

        val message = when (step) {
            WebRedirectStep.Loading -> "Web redirect is loading for '$paymentMethodType'"
            WebRedirectStep.Loaded -> "Web redirect has loaded for '$paymentMethodType'"
            WebRedirectStep.Dismissed -> "Payment for '$paymentMethodType' was dismissed by user"
            WebRedirectStep.Success -> "Payment for '$paymentMethodType' was successful"
        }
        coVerify(exactly = 1) {
            analyticsInteractor(
                MessageAnalyticsParams(
                    messageType = MessageType.INFO,
                    message = message,
                    severity = Severity.INFO,
                    diagnosticsId = "uuid"
                )
            )
            logReporter.info(message)
        }
        unmockkStatic(UUID::class)
    }

    companion object {
        @JvmStatic
        fun provideSteps() = listOf(
            WebRedirectStep.Loading,
            WebRedirectStep.Loaded,
            WebRedirectStep.Success,
            WebRedirectStep.Dismissed
        ).map { Arguments.of(it) }
    }
}
