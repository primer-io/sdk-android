package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateCustomerDetailsParams
import io.primer.android.domain.session.CachePolicy
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.domain.session.models.ConfigurationParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
        every { actionInteractor.invoke(any()) } returns flowOf(mockk())

        val result = delegate.invoke(
            firstName = "john",
            lastName = "doe",
            emailAddress = "john.doe@example.com"
        )

        assert(result.isSuccess)
        verify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify(exactly = 0) {
            actionInteractor.invoke(any())
        }
    }

    @Test
    fun `invoke() should patch client session if first name is changed`() = runTest {
        stubConfigurationInteractor()
        every { actionInteractor.invoke(any()) } returns flowOf(mockk())

        val result = delegate.invoke(
            firstName = "johnny",
            lastName = "doe",
            emailAddress = "john.doe@example.com"
        )

        assert(result.isSuccess)
        verify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify {
            actionInteractor.invoke(
                ActionUpdateCustomerDetailsParams(
                    firstName = "johnny",
                    lastName = null,
                    emailAddress = null
                )
            )
        }
    }

    @Test
    fun `invoke() should patch client session if last name is changed`() = runTest {
        stubConfigurationInteractor()
        every { actionInteractor.invoke(any()) } returns flowOf(mockk())

        val result = delegate.invoke(
            firstName = "john",
            lastName = "doee",
            emailAddress = "john.doe@example.com"
        )

        assert(result.isSuccess)
        verify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify {
            actionInteractor.invoke(
                ActionUpdateCustomerDetailsParams(
                    firstName = null,
                    lastName = "doee",
                    emailAddress = null
                )
            )
        }
    }

    @Test
    fun `invoke() should patch client session if email address is changed`() = runTest {
        stubConfigurationInteractor()
        every { actionInteractor.invoke(any()) } returns flowOf(mockk())

        val result = delegate.invoke(
            firstName = "john",
            lastName = "doe",
            emailAddress = "john.doe@example.org"
        )

        assert(result.isSuccess)
        verify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify {
            actionInteractor.invoke(
                ActionUpdateCustomerDetailsParams(
                    firstName = null,
                    lastName = null,
                    emailAddress = "john.doe@example.org"
                )
            )
        }
    }

    @Test
    fun `invoke() should return error if configuration is not cached`() = runTest {
        every { configurationInteractor.invoke(any()) } returns emptyFlow()

        val result = delegate.invoke(
            firstName = "john",
            lastName = "doe",
            emailAddress = "john.doe@example.org"
        )

        assert(result.isFailure)
        verify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify(exactly = 0) {
            actionInteractor.invoke(any())
        }
    }

    @Test
    fun `invoke() should return error if action interactor call fails`() = runTest {
        stubConfigurationInteractor()
        every { actionInteractor.invoke(any()) } returns flow { throw Exception() }

        val result = delegate.invoke(
            firstName = "john",
            lastName = "doe",
            emailAddress = "john.doe@example.org"
        )

        assert(result.isFailure)
        verify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        coVerify {
            actionInteractor.invoke(
                ActionUpdateCustomerDetailsParams(
                    firstName = null,
                    lastName = null,
                    emailAddress = "john.doe@example.org"
                )
            )
        }
    }

    private fun stubConfigurationInteractor() {
        every { configurationInteractor.invoke(any()) } returns flowOf(
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
