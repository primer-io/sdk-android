package io.primer.android.domain.rpc.retail_outlet

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.rpc.retailOutlets.RetailOutletFilterInteractor
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutlet
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutletFilterParams
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
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
class RetailOutletFilterInteractorTest {

    @RelaxedMockK
    internal lateinit var repository: RetailOutletRepository

    private lateinit var interactor: RetailOutletFilterInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = RetailOutletFilterInteractor(repository)
    }

    @Test
    fun `filterIssuingBanks() should return list of sorted and filtered banks IssuingBankRepository getCachedIssuingBanks() was success`() {
        val filter = "u"
        val params = RetailOutletFilterParams(filter)
        coEvery { repository.getCachedRetailOutlets() }.returns(flowOf(retailOutlets))
        runTest {
            val res = interactor(params).first()
            assertEquals(
                retailOutlets.sortedBy { it.name.lowercase() }
                    .filter { it.name.contains(filter) },
                res
            )
        }

        coVerify { repository.getCachedRetailOutlets() }
    }

    private companion object {
        val retailOutlets = listOf(
            RetailOutlet("1", "bunq", false, ""),
            RetailOutlet("2", "Retail2", false, ""),
            RetailOutlet("3", "Abn amro", false, ""),
        )
    }
}
