package io.primer.android.domain.rpc.bank

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.rpc.banks.BanksFilterInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankFilterParams
import io.primer.android.domain.rpc.banks.repository.IssuingBankRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class BanksFilterInteractorTest {

    @RelaxedMockK
    internal lateinit var repository: IssuingBankRepository

    private lateinit var interactor: BanksFilterInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor =
            BanksFilterInteractor(
                repository,
            )
    }

    @Test
    fun `filterIssuingBanks() should return list of sorted and filtered banks IssuingBankRepository getCachedIssuingBanks() was success`() {
        val filter = "u"
        val params = IssuingBankFilterParams(filter)
        coEvery { repository.getCachedIssuingBanks() }.returns(flowOf(banks))
        runTest {
            val res = interactor(params).first()
            assertEquals(
                banks.sortedBy { it.name.lowercase() }
                    .filter { it.name.contains(filter) },
                res
            )
        }

        coVerify { repository.getCachedIssuingBanks() }
    }

    private companion object {
        val banks = listOf(
            IssuingBank("1", "bunq", false, ""),
            IssuingBank("2", "Revolut", false, ""),
            IssuingBank("3", "Abn amro", false, ""),
        )
    }
}
