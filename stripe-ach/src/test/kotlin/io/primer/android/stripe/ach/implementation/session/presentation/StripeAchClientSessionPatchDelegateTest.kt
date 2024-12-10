package io.primer.android.stripe.ach.implementation.session.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateCustomerDetailsParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.ConfigurationParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class StripeAchClientSessionPatchDelegateTest {
    @MockK
    private lateinit var configurationInteractor: ConfigurationInteractor

    @MockK
    private lateinit var actionInteractor: ActionInteractor

    @InjectMockKs
    private lateinit var delegate: StripeAchClientSessionPatchDelegate

    @AfterEach
    fun tearDown() {
        confirmVerified(configurationInteractor, actionInteractor)
    }

    @Test
    fun `invoke() should not patch client session if first name, last name and email address are not changed`() = runTest {
        stubConfigurationInteractor()
        coEvery { actionInteractor.invoke(any()) } returns Result.success(mockk())

        val result = delegate.invoke(
            firstName = "john",
            lastName = "doe",
            emailAddress = "john.doe@example.com"
        )

        assert(result.isSuccess)
        coVerify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify(exactly = 0) {
            actionInteractor.invoke(any())
        }
    }

    @Test
    fun `invoke() should patch client session if first name is changed`() = runTest {
        stubConfigurationInteractor()
        coEvery { actionInteractor.invoke(any()) } returns Result.success(mockk())

        val result = delegate.invoke(
            firstName = "johnny",
            lastName = "doe",
            emailAddress = "john.doe@example.com"
        )

        assert(result.isSuccess)
        coVerify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify {
            actionInteractor.invoke(
                MultipleActionUpdateParams(
                    params = listOf(
                        ActionUpdateCustomerDetailsParams(
                            firstName = "johnny",
                            lastName = null,
                            emailAddress = null
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `invoke() should patch client session if last name is changed`() = runTest {
        stubConfigurationInteractor()
        coEvery { actionInteractor.invoke(any()) } returns Result.success(mockk())

        val result = delegate.invoke(
            firstName = "john",
            lastName = "doee",
            emailAddress = "john.doe@example.com"
        )

        assert(result.isSuccess)
        coVerify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify {
            actionInteractor.invoke(
                MultipleActionUpdateParams(
                    params = listOf(
                        ActionUpdateCustomerDetailsParams(
                            firstName = null,
                            lastName = "doee",
                            emailAddress = null
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `invoke() should patch client session if email address is changed`() = runTest {
        stubConfigurationInteractor()
        coEvery { actionInteractor.invoke(any()) } returns Result.success(mockk())

        val result = delegate.invoke(
            firstName = "john",
            lastName = "doe",
            emailAddress = "john.doe@example.org"
        )

        assert(result.isSuccess)
        coVerify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify {
            actionInteractor.invoke(
                MultipleActionUpdateParams(
                    params = listOf(
                        ActionUpdateCustomerDetailsParams(
                            firstName = null,
                            lastName = null,
                            emailAddress = "john.doe@example.org"
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `invoke() should return error if configuration is not cached`() = runTest {
        coEvery { configurationInteractor.invoke(any()) } returns Result.success(mockk())

        val result = delegate.invoke(
            firstName = "john",
            lastName = "doe",
            emailAddress = "john.doe@example.org"
        )

        assert(result.isFailure)
        coVerify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify(exactly = 0) {
            actionInteractor.invoke(any())
        }
    }

    @Test
    fun `invoke() should return error if action interactor call fails`() = runTest {
        stubConfigurationInteractor()
        coEvery { actionInteractor.invoke(any()) } returns Result.failure(Throwable())

        val result = delegate.invoke(
            firstName = "john",
            lastName = "doe",
            emailAddress = "john.doe@example.org"
        )

        assert(result.isFailure)
        coVerify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify {
            actionInteractor.invoke(
                MultipleActionUpdateParams(
                    params = listOf(
                        ActionUpdateCustomerDetailsParams(
                            firstName = null,
                            lastName = null,
                            emailAddress = "john.doe@example.org"
                        )
                    )
                )
            )
        }
    }

    private fun stubConfigurationInteractor() {
        coEvery { configurationInteractor.invoke(any()) } returns Result.success(
            mockk {
                every { clientSession.clientSessionDataResponse } returns mockk {
                    every { customer?.firstName } returns "john"
                    every { customer?.lastName } returns "doe"
                    every { customer?.emailAddress } returns "john.doe@example.com"
                }
            }
        )
    }
}
