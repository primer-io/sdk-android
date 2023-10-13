package io.primer.android.domain.rpc.retailOutlet

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.rpc.retailOutlets.RetailOutletInteractor
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutlet
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutletParams
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
class RetailOutletInteractorTest {

    @RelaxedMockK
    internal lateinit var repository: RetailOutletRepository

    private lateinit var interactor: RetailOutletInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = RetailOutletInteractor(repository)
    }

    @Test
    fun `getRetailOutlets() should return list of sorted banks RetailOutletRepository getRetailOutlets() was success`() {
        val params = mockk<RetailOutletParams>(relaxed = true)
        coEvery { repository.getRetailOutlets(any()) }.returns(flowOf(retailOutlets))

        runTest {
            val res = interactor(params).first()
            assertEquals(retailOutlets.sortedBy { it.name.lowercase() }, res)
        }

        coVerify { repository.getRetailOutlets(any()) }
    }

    private companion object {
        val retailOutlets = listOf(
            RetailOutlet("1", "bunq", false, ""),
            RetailOutlet("2", "Retail2", false, ""),
            RetailOutlet("3", "Abn amro", false, "")
        )
    }
}
