package io.primer.android.stripe.ach.implementation.mandate.presentation

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.core.extensions.toIso8601String
import io.primer.android.core.logging.internal.LogReporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Calendar
import java.util.UUID

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class StripeAchMandateTimestampLoggingDelegateTest {
    @MockK
    private lateinit var logReporter: LogReporter

    @MockK
    private lateinit var analyticsInteractor: AnalyticsInteractor

    @InjectMockKs
    private lateinit var delegate: StripeAchMandateTimestampLoggingDelegate

    @Test
    fun `logTimestamp() should log via analytics interactor and log reporter`() = runTest {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "UUID"
        every { logReporter.info(message = any()) } just Runs
        coEvery { analyticsInteractor(any()) } returns Result.success(Unit)

        val date = Calendar.getInstance().apply { set(2024, 4, 10) }.time
        delegate.logTimestamp(stripePaymentIntentId = "id", date = date)

        val message = "Stripe ACH mandate for payment intent with id " +
            "'id' was approved at '${date.toIso8601String()}'"
        coVerify {
            analyticsInteractor(
                MessageAnalyticsParams(
                    messageType = MessageType.INFO,
                    message = message,
                    severity = Severity.INFO,
                    diagnosticsId = "UUID"
                )
            )
            logReporter.info(message)
        }
        unmockkStatic(UUID::class)
    }
}
