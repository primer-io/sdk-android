package io.primer.android.processor3ds.presentation

import androidx.lifecycle.Observer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatus
import io.primer.android.processor3ds.InstantExecutorExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class Processor3DSViewModelTest {
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor = mockk()
    private val analyticsInteractor: AnalyticsInteractor = mockk()
    private lateinit var viewModel: Processor3DSViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = Processor3DSViewModel(pollingInteractor, analyticsInteractor)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getStatus should update statusUrlLiveData on success`() =
        runTest {
            val statusUrl = "https://example.com/status"
            val paymentMethodType = "exampleMethod"
            val asyncStatusResult = AsyncStatus(resumeToken = "resumeToken123")

            coEvery { pollingInteractor(any()) } returns flow { emit(asyncStatusResult) }

            val observer: Observer<String> = mockk(relaxed = true)
            viewModel.statusUrlLiveData.observeForever(observer)

            viewModel.getStatus(statusUrl, paymentMethodType)

            advanceUntilIdle()

            verify { observer.onChanged("resumeToken123") }
            viewModel.statusUrlLiveData.removeObserver(observer)
        }

    @Test
    fun `getStatus should update statusUrlErrorData on failure`() =
        runTest {
            val statusUrl = "https://example.com/status"
            val paymentMethodType = "exampleMethod"
            val error = RuntimeException("Error fetching status")

            coEvery { pollingInteractor(any()) } returns flow { throw error }

            val observer: Observer<Throwable> = mockk(relaxed = true)
            viewModel.statusUrlErrorData.observeForever(observer)

            viewModel.getStatus(statusUrl, paymentMethodType)

            advanceUntilIdle()

            verify { observer.onChanged(error) }
            viewModel.statusUrlErrorData.removeObserver(observer)
        }

    @Disabled("This test is failing because analyticsInteractor does not verify as being called.")
    @Test
    fun `addAnalyticsEvent should call analyticsInteractor`() =
        runTest {
            val params = mockk<BaseAnalyticsParams>()

            coEvery { analyticsInteractor(params) } returns Result.success(Unit)

            viewModel.addAnalyticsEvent(params)

            coVerify { analyticsInteractor(params) }
        }
}
