package io.primer.android.paymentmethods.analytics.delegate

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SdkAnalyticsValidationErrorLoggingDelegateTest {
    @RelaxedMockK
    private lateinit var analyticsInteractor: AnalyticsInteractor

    private lateinit var delegate: SdkAnalyticsValidationErrorLoggingDelegate

    @BeforeEach
    fun setUp() {
        delegate = SdkAnalyticsValidationErrorLoggingDelegate(analyticsInteractor)
    }

    @Test
    fun `logSdkAnalyticsError(PrimerValidationError) should log MessageAnalyticsParams via AnalyticsInteractor when called`() =
        runTest {
            val primerError =
                mockk<PrimerValidationError> {
                    every { errorId } returns "errorId"
                    every { description } returns "description"
                    every { diagnosticsId } returns "diagnosticsId"
                }
            coEvery { analyticsInteractor(any()) } returns Result.success(Unit)

            delegate.logSdkAnalyticsError(primerError)

            coVerify(exactly = 1) {
                analyticsInteractor(
                    MessageAnalyticsParams(
                        messageType = MessageType.VALIDATION_FAILED,
                        message = "description",
                        severity = Severity.WARN,
                        diagnosticsId = "diagnosticsId",
                        context = ErrorContextParams("errorId"),
                    ),
                )
            }
        }

    @Test
    fun `logSdkAnalyticsError(PrimerError) should log MessageAnalyticsParams via AnalyticsInteractor when called`() =
        runTest {
            val context = ErrorContextParams("errorId", "paymentMethodType")
            val primerError =
                mockk<PrimerError> {
                    every { errorId } returns "errorId"
                    every { description } returns "description"
                    every { diagnosticsId } returns "diagnosticsId"
                    every { this@mockk.context } returns context
                }
            coEvery { analyticsInteractor(any()) } returns Result.success(Unit)

            delegate.logSdkAnalyticsError(primerError)

            coVerify(exactly = 1) {
                analyticsInteractor(
                    MessageAnalyticsParams(
                        messageType = MessageType.VALIDATION_FAILED,
                        message = "description",
                        severity = Severity.WARN,
                        diagnosticsId = "diagnosticsId",
                        context = context,
                    ),
                )
            }
        }

    @Test
    fun `logSdkAnalyticsError(PrimerError) should log MessageAnalyticsParams via AnalyticsInteractor when called with null context`() =
        runTest {
            val primerError =
                mockk<PrimerError> {
                    every { errorId } returns "errorId"
                    every { description } returns "description"
                    every { diagnosticsId } returns "diagnosticsId"
                    every { this@mockk.context } returns null
                }
            coEvery { analyticsInteractor(any()) } returns Result.success(Unit)

            delegate.logSdkAnalyticsError(primerError)

            coVerify(exactly = 1) {
                analyticsInteractor(
                    MessageAnalyticsParams(
                        messageType = MessageType.VALIDATION_FAILED,
                        message = "description",
                        severity = Severity.WARN,
                        diagnosticsId = "diagnosticsId",
                        context = ErrorContextParams("errorId"),
                    ),
                )
            }
        }
}
