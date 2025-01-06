package io.primer.android.stripe.ach.implementation.session.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.ConfigurationParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun `invoke() should return first name, last name and email address when configuration is cached`() =
        runTest {
            coEvery { configurationInteractor.invoke(any()) } returns
                Result.success(
                    mockk {
                        every { clientSession.clientSessionDataResponse } returns
                            mockk {
                                every { customer?.firstName } returns "john"
                                every { customer?.lastName } returns "doe"
                                every { customer?.emailAddress } returns "john.doe@example.com"
                            }
                    },
                )

            val result = delegate.invoke()

            assertEquals(
                Result.success(
                    GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                        firstName = "john",
                        lastName = "doe",
                        emailAddress = "john.doe@example.com",
                    ),
                ),
                result,
            )
            coVerify {
                configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
            }
            confirmVerified(configurationInteractor)
        }

    @Test
    fun `invoke() should return blank strings when customer object is null`() =
        runTest {
            coEvery { configurationInteractor.invoke(any()) } returns
                Result.success(
                    mockk {
                        every { clientSession.clientSessionDataResponse } returns
                            mockk {
                                every { customer } returns null
                            }
                    },
                )

            val result = delegate.invoke()

            assertEquals(
                Result.success(
                    GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                        firstName = "",
                        lastName = "",
                        emailAddress = "",
                    ),
                ),
                result,
            )
            coVerify {
                configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
            }
            confirmVerified(configurationInteractor)
        }

    @Test
    fun `invoke() should return error if configuration is not cached`() =
        runTest {
            coEvery { configurationInteractor.invoke(any()) } returns Result.failure(Throwable())

            val result = delegate.invoke()

            assert(result.isFailure)
            coVerify {
                configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache))
            }
            confirmVerified(configurationInteractor)
        }
}
