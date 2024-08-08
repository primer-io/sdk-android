package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.domain.session.CachePolicy
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.domain.session.models.ConfigurationParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class GetClientSessionCustomerDetailsDelegateTest {
    @MockK
    private lateinit var configurationInteractor: ConfigurationInteractor

    @InjectMockKs
    private lateinit var delegate: GetClientSessionCustomerDetailsDelegate

    @Test
    fun `invoke() should return first name, last name and email address when configuration is cached`() = runTest {
        every { configurationInteractor.invoke(any()) } returns flowOf(
            mockk {
                every { clientSession.clientSessionDataResponse } returns mockk {
                    every { customer?.firstName } returns "john"
                    every { customer?.lastName } returns "doe"
                    every { customer?.emailAddress } returns "john.doe@example.com"
                }
            }
        )

        val result = delegate.invoke()

        assertEquals(
            Result.success(
                GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                    firstName = "john",
                    lastName = "doe",
                    emailAddress = "john.doe@example.com"
                )
            ),
            result
        )
        verify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        confirmVerified(configurationInteractor)
    }

    @Test
    fun `invoke() should return blank strings when customer object is null`() = runTest {
        every { configurationInteractor.invoke(any()) } returns flowOf(
            mockk {
                every { clientSession.clientSessionDataResponse } returns mockk {
                    every { customer } returns null
                }
            }
        )

        val result = delegate.invoke()

        assertEquals(
            Result.success(
                GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                    firstName = "",
                    lastName = "",
                    emailAddress = ""
                )
            ),
            result
        )
        verify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        confirmVerified(configurationInteractor)
    }

    @Test
    fun `invoke() should return error if configuration is not cached`() = runTest {
        every { configurationInteractor.invoke(any()) } returns emptyFlow()

        val result = delegate.invoke()

        assert(result.isFailure)
        verify {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
        }
        confirmVerified(configurationInteractor)
    }
}
