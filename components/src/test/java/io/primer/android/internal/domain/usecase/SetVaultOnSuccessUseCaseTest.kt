package io.primer.android.internal.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateVaultOnSuccessParams
import io.primer.android.configuration.domain.model.ClientSessionData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetVaultOnSuccessUseCaseTest {

    private lateinit var useCase: SetVaultOnSuccessUseCase
    private lateinit var mockActionInteractor: ActionInteractor

    @BeforeEach
    fun setUp() {
        mockActionInteractor = mockk()
        useCase = SetVaultOnSuccessUseCase(mockActionInteractor)
    }

    @Test
    fun `invoke should call actionInteractor with ActionUpdateVaultOnSuccessParams when vaultOnSuccess is true`() =
        runTest {
            val clientSessionData = mockk<ClientSessionData>()
            coEvery { mockActionInteractor.invoke(any()) } returns Result.success(clientSessionData)

            val result = useCase(true)

            assertTrue(result.isSuccess)
            assertEquals(clientSessionData, result.getOrNull())
            coVerify(exactly = 1) {
                mockActionInteractor.invoke(
                    withArg { params ->
                        assertEquals(1, params.params.size)
                        val vaultParam = params.params.first() as ActionUpdateVaultOnSuccessParams
                        assertTrue(vaultParam.vaultOnSuccess)
                    },
                )
            }
        }

    @Test
    fun `invoke should call actionInteractor with ActionUpdateVaultOnSuccessParams when vaultOnSuccess is false`() =
        runTest {
            val clientSessionData = mockk<ClientSessionData>()
            coEvery { mockActionInteractor.invoke(any()) } returns Result.success(clientSessionData)

            val result = useCase(false)

            assertTrue(result.isSuccess)
            assertEquals(clientSessionData, result.getOrNull())
            coVerify(exactly = 1) {
                mockActionInteractor.invoke(
                    withArg { params ->
                        assertEquals(1, params.params.size)
                        val vaultParam = params.params.first() as ActionUpdateVaultOnSuccessParams
                        assertTrue(!vaultParam.vaultOnSuccess)
                    },
                )
            }
        }

    @Test
    fun `invoke should return failure when actionInteractor fails`() = runTest {
        val error = Exception("Network error")
        coEvery { mockActionInteractor.invoke(any()) } returns Result.failure(error)

        val result = useCase(true)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
