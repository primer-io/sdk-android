package io.primer.android.domain.rpc.bank

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.rpc.banks.BanksInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankParams
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
class BanksInteractorTest {

    @RelaxedMockK
    internal lateinit var repository: IssuingBankRepository

    private lateinit var interactor: BanksInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor =
            BanksInteractor(
                repository,
            )
    }

    @Test
    fun `getIssuingBanks() should return list of sorted banks IssuingBankRepository getIssuingBanks() was success`() {
        val params = mockk<IssuingBankParams>(relaxed = true)
        coEvery { repository.getIssuingBanks(any()) }.returns(flowOf(banks))

        runTest {
            val res = interactor(params).first()
            assertEquals(banks.sortedBy { it.name.lowercase() }, res)
        }

        coVerify { repository.getIssuingBanks(any()) }
    }

    private companion object {
        val banks = listOf(
            IssuingBank("1", "bunq", false, ""),
            IssuingBank("2", "Revolut", false, ""),
            IssuingBank("3", "Abn amro", false, ""),
        )
    }
}
