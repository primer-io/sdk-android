package io.primer.android.viewmodel

import com.jraska.livedata.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.rpc.banks.BanksInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.BankItem
import io.primer.android.viewmodel.bank.BankSelectionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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

    private lateinit var viewModel: BankSelectionViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = BankSelectionViewModel(interactor)
    }

    @Test
    fun `loadData() should receive list of base bank items when getIssuingBanks was success`() {
        val bank = mockk<IssuingBank>(relaxed = true)
        val asyncPaymentMethodDescriptor = mockk<AsyncPaymentMethodDescriptor>(relaxed = true)

        val observer = viewModel.itemsLiveData.test()
        every { interactor(any()) }.returns(
            flowOf(
                listOf(bank)
            )
        )

        viewModel.loadData(asyncPaymentMethodDescriptor)

        verify { interactor(any()) }
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
        every { interactor(any()) }.returns(
            flow { throw Exception() }
        )

        viewModel.loadData(asyncPaymentMethodDescriptor)

        verify { interactor(any()) }
        observer.assertValue(Unit)
    }

    @Test
    fun `loadData() should receive loading event when getIssuingBanks completed`() {
        val asyncPaymentMethodDescriptor = mockk<AsyncPaymentMethodDescriptor>(relaxed = true)

        val observer = viewModel.loadingLiveData.test()
        every { interactor(any()) }.returns(
            flow { throw Exception() }
        )

        viewModel.loadData(asyncPaymentMethodDescriptor)

        verify { interactor(any()) }
        observer.assertValue(false)
    }
}
