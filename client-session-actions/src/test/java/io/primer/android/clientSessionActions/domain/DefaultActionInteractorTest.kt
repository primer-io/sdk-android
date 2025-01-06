package io.primer.android.clientSessionActions.domain

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.clientSessionActions.domain.handlers.CheckoutClientSessionActionsHandler
import io.primer.android.clientSessionActions.domain.models.BaseActionUpdateParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.clientSessionActions.domain.repository.ActionRepository
import io.primer.android.clientSessionActions.domain.validator.ActionUpdateFilter
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.domain.model.ClientSessionData
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.BaseErrorResolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class DefaultActionInteractorTest {
    private lateinit var actionRepository: ActionRepository
    private lateinit var configurationRepository: ConfigurationRepository
    private lateinit var actionUpdateFilter: ActionUpdateFilter
    private lateinit var errorEventResolver: BaseErrorResolver
    private lateinit var clientSessionActionsHandler: CheckoutClientSessionActionsHandler
    private lateinit var defaultActionInteractor: DefaultActionInteractor

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        actionRepository = mockk()
        configurationRepository = mockk()
        actionUpdateFilter = mockk()
        errorEventResolver = mockk()
        clientSessionActionsHandler = mockk(relaxed = true)

        every { configurationRepository.getConfiguration() } returns
            mockk<Configuration> {
                every { clientSession.clientSessionDataResponse.toClientSessionData() } returns mockk()
            }
    }

    private fun createDefaultActionInteractor(ignoreErrors: Boolean = false) =
        DefaultActionInteractor(
            actionRepository = actionRepository,
            configurationRepository = configurationRepository,
            actionUpdateFilter = actionUpdateFilter,
            errorEventResolver = errorEventResolver,
            clientSessionActionsHandler = clientSessionActionsHandler,
            ignoreErrors = ignoreErrors,
            dispatcher = testDispatcher,
        )

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `performAction executes updateClientActions and calls onSuccess on valid params`() {
        defaultActionInteractor = createDefaultActionInteractor()
        val params = mockk<BaseActionUpdateParams>()
        val primerClientSession = mockk<PrimerClientSession>()
        val clientSessionData =
            mockk<ClientSessionData> {
                every { clientSession } returns primerClientSession
            }
        val configurationData = mockk<ConfigurationData>()

        every { actionUpdateFilter.filter(params) } returns false
        coEvery { actionRepository.updateClientActions(listOf(params)) } returns Result.success(clientSessionData)
        every { configurationData.clientSession.paymentMethod?.surcharges.orEmpty() } returns emptyMap()

        runTest {
            val result = defaultActionInteractor(MultipleActionUpdateParams(listOf(params)))
            assertTrue(result.isSuccess)
        }

        coVerify {
            clientSessionActionsHandler.onClientSessionUpdateStarted()
            clientSessionActionsHandler.onClientSessionUpdateSuccess(any())
        }
    }

    @Test
    fun `performAction returns cached client session data on invalid params`() {
        defaultActionInteractor = createDefaultActionInteractor()
        val params = mockk<BaseActionUpdateParams>()

        val primerClientSession = mockk<PrimerClientSession>()
        val clientSessionData =
            mockk<ClientSessionData> {
                every { clientSession } returns primerClientSession
            }
        val configurationData = mockk<Configuration>()

        coEvery { actionRepository.updateClientActions(listOf(params)) } returns Result.success(clientSessionData)
        every { actionUpdateFilter.filter(params) } returns true
        every { configurationRepository.getConfiguration() } returns configurationData
        every {
            configurationData.clientSession.clientSessionDataResponse.toClientSessionData()
        } returns clientSessionData

        runTest {
            val result = defaultActionInteractor(MultipleActionUpdateParams(listOf(params)))
            assertTrue(result.isSuccess)
        }

        coVerify {
            configurationRepository.getConfiguration()
        }
        coVerify(exactly = 0) {
            actionRepository.updateClientActions(listOf(params))
        }
    }

    @Test
    fun `performAction calls onClientSessionUpdateError on failure if ignoreErrors is false`() =
        runTest {
            defaultActionInteractor = createDefaultActionInteractor(ignoreErrors = false)
            val params = mockk<BaseActionUpdateParams>()
            val error = RuntimeException("Test error")
            val primerError = mockk<PrimerError>()

            every { actionUpdateFilter.filter(params) } returns false
            coEvery { actionRepository.updateClientActions(listOf(params)) } returns Result.failure(error)
            every { errorEventResolver.resolve(error) } returns primerError

            val result = defaultActionInteractor(MultipleActionUpdateParams(listOf(params)))

            assertTrue(result.isFailure)
            coVerify {
                clientSessionActionsHandler.onClientSessionUpdateStarted()
                clientSessionActionsHandler.onClientSessionUpdateError(primerError)
            }
        }

    @Test
    fun `performAction does not call onClientSessionUpdateError on failure if ignoreErrors is true`() =
        runTest {
            defaultActionInteractor = createDefaultActionInteractor(ignoreErrors = true)
            val params = mockk<BaseActionUpdateParams>()
            val error = RuntimeException("Test error")
            val primerError = mockk<PrimerError>()

            every { actionUpdateFilter.filter(params) } returns false
            coEvery { actionRepository.updateClientActions(listOf(params)) } returns Result.failure(error)
            every { errorEventResolver.resolve(error) } returns primerError

            val result = defaultActionInteractor(MultipleActionUpdateParams(listOf(params)))

            assertTrue(result.isFailure)
            coVerify {
                clientSessionActionsHandler.onClientSessionUpdateStarted()
            }
            coVerify(exactly = 0) {
                clientSessionActionsHandler.onClientSessionUpdateError(primerError)
            }
        }
}
