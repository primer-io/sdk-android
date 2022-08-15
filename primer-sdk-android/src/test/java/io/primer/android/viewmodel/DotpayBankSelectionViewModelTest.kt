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
import io.primer.android.domain.rpc.banks.BanksFilterInteractor
import io.primer.android.domain.rpc.banks.BanksInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.ui.BankItem
import io.primer.android.viewmodel.bank.DotPayBankSelectionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class DotpayBankSelectionViewModelTest {

    @RelaxedMockK
    internal lateinit var interactor: BanksInteractor

    @RelaxedMockK
    internal lateinit var banksFilterInteractor: BanksFilterInteractor

    @RelaxedMockK
    internal lateinit var analyticsInteractor: AnalyticsInteractor

    private lateinit var viewModel: DotPayBankSelectionViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = DotPayBankSelectionViewModel(
            interactor,
            banksFilterInteractor,
            analyticsInteractor
        )
    }

    @Test
    fun `onFilterChanged() should receive list of base bank items when filterIssuingBanks was success`() {
        val bank = mockk<IssuingBank>(relaxed = true)
        val observer = viewModel.itemsLiveData.test()
        coEvery { banksFilterInteractor(any()) }.returns(
            flowOf(
                listOf(bank)
            )
        )

        runTest {
            viewModel.onFilterChanged("")
        }

        coVerify { banksFilterInteractor(any()) }

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
}
