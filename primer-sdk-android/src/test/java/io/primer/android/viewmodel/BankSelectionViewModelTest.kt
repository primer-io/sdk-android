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
import io.primer.android.domain.rpc.banks.BanksInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.BankItem
import io.primer.android.viewmodel.bank.BankSelectionViewModel
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
class BankSelectionViewModelTest {

    @RelaxedMockK
    internal lateinit var interactor: BanksInteractor

    @RelaxedMockK
    internal lateinit var analyticsInteractor: AnalyticsInteractor

    private lateinit var viewModel: BankSelectionViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = BankSelectionViewModel(interactor, analyticsInteractor)
    }

    @Test
    fun `loadData() should receive list of base bank items when getIssuingBanks was success`() {
        val bank = mockk<IssuingBank>(relaxed = true)
        val asyncPaymentMethodDescriptor = mockk<AsyncPaymentMethodDescriptor>(relaxed = true)

        val observer = viewModel.itemsLiveData.test()
        coEvery { interactor(any()) }.returns(
            flowOf(
                listOf(bank)
            )
        )

        runTest {
            viewModel.loadData(asyncPaymentMethodDescriptor)
        }

        coVerify { interactor(any()) }

        observer.assertValue(
            listOf(
                BankItem(
                    bank.id,
                    bank.name,
                    bank.iconUrl
                )
            )
        )
    }

    @Test
    fun `loadData() should receive error event when getIssuingBanks failed`() {
        val asyncPaymentMethodDescriptor = mockk<AsyncPaymentMethodDescriptor>(relaxed = true)

        val observer = viewModel.errorLiveData.test()
        coEvery { interactor(any()) }.returns(
            flow { throw Exception() }
        )

        runTest {
            viewModel.loadData(asyncPaymentMethodDescriptor)
        }

        coVerify { interactor(any()) }

        observer.assertValue(Unit)
    }

    @Test
    fun `loadData() should receive loading event when getIssuingBanks completed`() {
        val asyncPaymentMethodDescriptor = mockk<AsyncPaymentMethodDescriptor>(relaxed = true)

        val observer = viewModel.loadingLiveData.test()
        coEvery { interactor(any()) }.returns(
            flow { throw Exception() }
        )
        runTest {
            viewModel.loadData(asyncPaymentMethodDescriptor)
        }

        coVerify { interactor(any()) }
        observer.assertValue(false)
    }
}
