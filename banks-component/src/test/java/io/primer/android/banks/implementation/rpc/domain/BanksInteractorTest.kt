package io.primer.android.banks.implementation.rpc.domain

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBank
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBankParams
import io.primer.android.banks.implementation.rpc.domain.repository.IssuingBankRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        interactor = BanksInteractor(repository)
    }

    @Test
    fun `getIssuingBanks() should return list of sorted banks when IssuingBankRepository getIssuingBanks() was success`() {
        val params = mockk<IssuingBankParams>(relaxed = true)
        coEvery { repository.getIssuingBanks(any()) } returns Result.success(banks)

        runTest {
            val res = interactor(params).getOrThrow()
            assertEquals(banks.sortedBy { it.name.lowercase() }, res)
        }

        coVerify { repository.getIssuingBanks(any()) }
    }

    @Test
    fun `getIssuingBanks() should return exception when IssuingBankRepository getIssuingBanks() was failure`() {
        val exception = mockk<Exception>()
        val params = mockk<IssuingBankParams>(relaxed = true)
        coEvery { repository.getIssuingBanks(any()) } returns Result.failure(exception)

        runTest {
            val res = interactor(params).exceptionOrNull()
            assertEquals(exception, res)
        }

        coVerify { repository.getIssuingBanks(any()) }
    }

    private companion object {
        val banks = listOf(
            IssuingBank("1", "bunq", false, ""),
            IssuingBank("2", "Revolut", false, ""),
            IssuingBank("3", "Abn amro", false, "")
        )
    }
}
