package io.primer.android.viewmodel

import com.jraska.livedata.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor
import io.primer.android.domain.payments.async.models.AsyncStatus
import io.primer.android.presentation.payment.async.AsyncPaymentMethodViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.component.KoinApiExtension

@ExperimentalCoroutinesApi
@KoinApiExtension
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class AsyncPaymentMethodViewModelTest {

    @RelaxedMockK
    internal lateinit var interactor: AsyncPaymentMethodInteractor

    @RelaxedMockK
    internal lateinit var analyticsInteractor: AnalyticsInteractor

    private lateinit var viewModel: AsyncPaymentMethodViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = AsyncPaymentMethodViewModel(interactor, analyticsInteractor)
    }

    @Test
    fun `getStatus() should receive finish event getPaymentFlowStatus was success`() {
        val asyncStatus = mockk<AsyncStatus>(relaxed = true)
        val observer = viewModel.statusUrlLiveData.test()
        coEvery { interactor(any()) }.returns(flowOf(asyncStatus))

        runTest {
            viewModel.getStatus("", PaymentMethodType.HOOLAH.name)
        }

        coVerify { interactor(any()) }

        observer.assertValue(Unit)
    }

    @Test
    fun `getStatus() should receive error event getPaymentFlowStatus failed`() {
        val observer = viewModel.statusUrlErrorData.test()
        coEvery { interactor(any()) }.returns(flow { throw Exception() })

        runTest {
            viewModel.getStatus("", PaymentMethodType.HOOLAH.name)
        }

        coVerify { interactor(any()) }

        observer.assertValue(Unit)
    }
}
