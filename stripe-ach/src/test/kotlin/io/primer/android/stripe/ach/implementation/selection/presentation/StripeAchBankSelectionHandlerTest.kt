package io.primer.android.stripe.ach.implementation.selection.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.configuration.mock.presentation.MockConfigurationDelegate
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.errors.data.exception.IllegalValueException
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.stripe.StripeBankAccountCollectorActivity
import io.primer.android.stripe.ach.api.additionalInfo.AchAdditionalInfo
import io.primer.android.stripe.ach.implementation.session.data.exception.StripeIllegalValueKey
import io.primer.android.stripe.ach.implementation.session.presentation.GetClientSessionCustomerDetailsDelegate
import io.primer.android.stripe.ach.implementation.session.presentation.GetStripePublishableKeyDelegate
import io.primer.android.stripe.exceptions.StripeSdkException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class StripeAchBankSelectionHandlerTest {
    private val clientSecret = "clientSecret"

    @MockK
    private lateinit var contextProvider: () -> Context

    @MockK
    private lateinit var checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler

    @MockK
    private lateinit var stripePublishableKeyDelegate: GetStripePublishableKeyDelegate

    @MockK
    private lateinit var getClientSessionCustomerDetailsDelegate: GetClientSessionCustomerDetailsDelegate

    @MockK
    private lateinit var mockConfigurationDelegate: MockConfigurationDelegate

    @InjectMockKs
    private lateinit var handler: StripeAchBankSelectionHandler

    @AfterEach
    fun tearDown() {
        confirmVerified(
            contextProvider,
            stripePublishableKeyDelegate,
            getClientSessionCustomerDetailsDelegate,
            mockConfigurationDelegate,
        )
    }

    @Test
    fun `fetchSelectedBankId() should return error if the Stripe Publishable Key delegate returns error`() =
        runTest {
            every { stripePublishableKeyDelegate.invoke() } returns Result.failure(Throwable())

            val result = handler.fetchSelectedBankId(clientSecret)

            verify {
                stripePublishableKeyDelegate.invoke()
            }
            assertEquals(
                IllegalValueException(
                    key = StripeIllegalValueKey.MISSING_PUBLISHABLE_KEY,
                    message = "Required value for ${StripeIllegalValueKey.MISSING_PUBLISHABLE_KEY.key} was null.",
                ),
                result.exceptionOrNull(),
            )
        }

    @Test
    fun `fetchSelectedBankId() should return error if the customer details delegate returns error`() =
        runTest {
            every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
            val error = Exception()
            coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns Result.failure(error)

            val result = handler.fetchSelectedBankId(clientSecret)

            verify {
                stripePublishableKeyDelegate.invoke()
            }
            coVerify {
                getClientSessionCustomerDetailsDelegate.invoke()
            }
            assertEquals(error, result.exceptionOrNull())
        }

    @Test
    fun `fetchSelectedBankId() should trigger the bank collection flow`() =
        runTest {
            every { mockConfigurationDelegate.isMockedFlow() } returns false
            every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
            val onAdditionalInfoReceivedSlot = slot<AchAdditionalInfo.ProvideActivityResultRegistry>()
            coEvery { checkoutAdditionalInfoHandler.handle(capture(onAdditionalInfoReceivedSlot)) } just Runs
            coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns
                Result.success(
                    GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                        firstName = "John",
                        lastName = "Doe",
                        emailAddress = "john@doe.com",
                    ),
                )
            val activityResultRegistry = mockk<ActivityResultRegistry>()
            val activityResultCallbackSlot = slot<ActivityResultCallback<ActivityResult>>()
            mockkObject(StripeBankAccountCollectorActivity.Companion)
            val intent = mockk<Intent>()
            every { StripeBankAccountCollectorActivity.getLaunchIntent(any(), any()) } returns intent
            val activityResultLauncher = mockk<ActivityResultLauncher<Intent>>()
            every { activityResultLauncher.launch(any()) } just Runs
            every {
                activityResultRegistry.register(
                    any(),
                    any<ActivityResultContracts.StartActivityForResult>(),
                    capture(activityResultCallbackSlot),
                )
            } returns activityResultLauncher
            val activityResult =
                mockk<ActivityResult> {
                    every { resultCode } returns Activity.RESULT_OK
                    every { data?.extras?.getString(any()) } returns "paymentMethodId"
                }

            val job =
                async {
                    handler.fetchSelectedBankId(clientSecret)
                }
            delay(1.seconds)
            val context = mockk<Context>()
            every { contextProvider.invoke() } returns context
            launch {
                (onAdditionalInfoReceivedSlot.captured).provide(activityResultRegistry)
            }
            delay(1.seconds)
            launch {
                activityResultCallbackSlot.captured.onActivityResult(activityResult)
            }

            val result = job.await()
            assertEquals("paymentMethodId", result.getOrNull())
            verify {
                mockConfigurationDelegate.isMockedFlow()
                stripePublishableKeyDelegate.invoke()
                activityResultRegistry.register(
                    "bank_collector",
                    any<ActivityResultContracts.StartActivityForResult>(),
                    any(),
                )
                contextProvider.invoke()
                StripeBankAccountCollectorActivity.getLaunchIntent(
                    context = context,
                    params =
                    StripeBankAccountCollectorActivity.Params(
                        fullName = "John Doe",
                        emailAddress = "john@doe.com",
                        publishableKey = "pk",
                        clientSecret = clientSecret,
                    ),
                )
                activityResultLauncher.launch(intent)
                activityResult.resultCode
                activityResult.data?.extras?.getString(
                    StripeBankAccountCollectorActivity.PAYMENT_METHOD_ID,
                )
            }
            coVerify {
                getClientSessionCustomerDetailsDelegate.invoke()
                checkoutAdditionalInfoHandler.handle(any())
            }
            confirmVerified(
                activityResultRegistry,
                activityResultLauncher,
                activityResult,
                context,
            )
            unmockkObject(StripeBankAccountCollectorActivity.Companion)
        }

    @Test
    fun `fetchSelectedBankId() should return error when bank collection flow is cancelled`() =
        runTest {
            every { mockConfigurationDelegate.isMockedFlow() } returns false
            every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
            val onAdditionalInfoReceivedSlot = slot<AchAdditionalInfo.ProvideActivityResultRegistry>()
            coEvery { checkoutAdditionalInfoHandler.handle(capture(onAdditionalInfoReceivedSlot)) } just Runs
            coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns
                Result.success(
                    GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                        firstName = "John",
                        lastName = "Doe",
                        emailAddress = "john@doe.com",
                    ),
                )
            val activityResultRegistry = mockk<ActivityResultRegistry>()
            val activityResultCallbackSlot = slot<ActivityResultCallback<ActivityResult>>()
            mockkObject(StripeBankAccountCollectorActivity.Companion)
            val intent = mockk<Intent>()
            every { StripeBankAccountCollectorActivity.getLaunchIntent(any(), any()) } returns intent
            val activityResultLauncher = mockk<ActivityResultLauncher<Intent>>()
            every { activityResultLauncher.launch(any()) } just Runs
            every {
                activityResultRegistry.register(
                    any(),
                    any<ActivityResultContracts.StartActivityForResult>(),
                    capture(activityResultCallbackSlot),
                )
            } returns activityResultLauncher
            val activityResult =
                mockk<ActivityResult> {
                    every { resultCode } returns Activity.RESULT_CANCELED
                }

            val job =
                async {
                    handler.fetchSelectedBankId(clientSecret)
                }
            delay(1.seconds)
            val context = mockk<Context>()
            every { contextProvider.invoke() } returns context
            launch {
                (onAdditionalInfoReceivedSlot.captured).provide(activityResultRegistry)
            }
            delay(1.seconds)
            launch {
                activityResultCallbackSlot.captured.onActivityResult(activityResult)
            }

            val result = job.await()
            assertEquals(
                PaymentMethodCancelledException(paymentMethodType = PaymentMethodType.STRIPE_ACH.name),
                result.exceptionOrNull(),
            )
            verify {
                mockConfigurationDelegate.isMockedFlow()
                stripePublishableKeyDelegate.invoke()
                activityResultRegistry.register(
                    "bank_collector",
                    any<ActivityResultContracts.StartActivityForResult>(),
                    any(),
                )
                contextProvider.invoke()
                StripeBankAccountCollectorActivity.getLaunchIntent(
                    context = context,
                    params =
                    StripeBankAccountCollectorActivity.Params(
                        fullName = "John Doe",
                        emailAddress = "john@doe.com",
                        publishableKey = "pk",
                        clientSecret = clientSecret,
                    ),
                )
                activityResultLauncher.launch(intent)
                activityResult.resultCode
            }
            coVerify {
                getClientSessionCustomerDetailsDelegate.invoke()
            }
            confirmVerified(
                activityResultRegistry,
                activityResultLauncher,
                activityResult,
                context,
            )
            unmockkObject(StripeBankAccountCollectorActivity.Companion)
        }

    @Test
    fun `fetchSelectedBankId() should return error when bank collection flow errors`() =
        runTest {
            every { mockConfigurationDelegate.isMockedFlow() } returns false
            every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
            val onAdditionalInfoReceivedSlot = slot<AchAdditionalInfo.ProvideActivityResultRegistry>()
            coEvery { checkoutAdditionalInfoHandler.handle(capture(onAdditionalInfoReceivedSlot)) } just Runs
            coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns
                Result.success(
                    GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                        firstName = "John",
                        lastName = "Doe",
                        emailAddress = "john@doe.com",
                    ),
                )
            val activityResultRegistry = mockk<ActivityResultRegistry>()
            val activityResultCallbackSlot = slot<ActivityResultCallback<ActivityResult>>()
            mockkObject(StripeBankAccountCollectorActivity.Companion)
            val intent = mockk<Intent>()
            every { StripeBankAccountCollectorActivity.getLaunchIntent(any(), any()) } returns intent
            val activityResultLauncher = mockk<ActivityResultLauncher<Intent>>()
            every { activityResultLauncher.launch(any()) } just Runs
            every {
                activityResultRegistry.register(
                    any(),
                    any<ActivityResultContracts.StartActivityForResult>(),
                    capture(activityResultCallbackSlot),
                )
            } returns activityResultLauncher
            val exception = Exception()
            val activityResult =
                mockk<ActivityResult> {
                    every { resultCode } returns StripeBankAccountCollectorActivity.RESULT_ERROR
                    every { data?.extras?.getSerializable(any()) } returns exception
                }

            val job =
                async {
                    handler.fetchSelectedBankId(clientSecret)
                }
            delay(1.seconds)
            val context = mockk<Context>()
            every { contextProvider.invoke() } returns context
            launch {
                (onAdditionalInfoReceivedSlot.captured).provide(activityResultRegistry)
            }
            delay(1.seconds)
            launch {
                activityResultCallbackSlot.captured.onActivityResult(activityResult)
            }

            val result = job.await()
            assertEquals(exception, result.exceptionOrNull())
            verify {
                mockConfigurationDelegate.isMockedFlow()
                stripePublishableKeyDelegate.invoke()
                activityResultRegistry.register(
                    "bank_collector",
                    any<ActivityResultContracts.StartActivityForResult>(),
                    any(),
                )
                contextProvider.invoke()
                StripeBankAccountCollectorActivity.getLaunchIntent(
                    context = context,
                    params =
                    StripeBankAccountCollectorActivity.Params(
                        fullName = "John Doe",
                        emailAddress = "john@doe.com",
                        publishableKey = "pk",
                        clientSecret = clientSecret,
                    ),
                )
                activityResultLauncher.launch(intent)
                activityResult.resultCode
                activityResult.data
            }
            coVerify {
                getClientSessionCustomerDetailsDelegate.invoke()
            }
            confirmVerified(
                activityResultRegistry,
                activityResultLauncher,
                activityResult,
                context,
            )
            unmockkObject(StripeBankAccountCollectorActivity.Companion)
        }

    @Test
    fun `fetchSelectedBankId() should return error when bank collection flow errors but the exception is null`() =
        runTest {
            every { mockConfigurationDelegate.isMockedFlow() } returns false
            every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
            val onAdditionalInfoReceivedSlot = slot<AchAdditionalInfo.ProvideActivityResultRegistry>()
            coEvery { checkoutAdditionalInfoHandler.handle(capture(onAdditionalInfoReceivedSlot)) } just Runs
            coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns
                Result.success(
                    GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                        firstName = "John",
                        lastName = "Doe",
                        emailAddress = "john@doe.com",
                    ),
                )
            val activityResultRegistry = mockk<ActivityResultRegistry>()
            val activityResultCallbackSlot = slot<ActivityResultCallback<ActivityResult>>()
            mockkObject(StripeBankAccountCollectorActivity.Companion)
            val intent = mockk<Intent>()
            every { StripeBankAccountCollectorActivity.getLaunchIntent(any(), any()) } returns intent
            val activityResultLauncher = mockk<ActivityResultLauncher<Intent>>()
            every { activityResultLauncher.launch(any()) } just Runs
            every {
                activityResultRegistry.register(
                    any(),
                    any<ActivityResultContracts.StartActivityForResult>(),
                    capture(activityResultCallbackSlot),
                )
            } returns activityResultLauncher
            val activityResult =
                mockk<ActivityResult> {
                    every { resultCode } returns StripeBankAccountCollectorActivity.RESULT_ERROR
                    every { data?.extras?.getSerializable(any()) } returns null
                }

            val job =
                async {
                    handler.fetchSelectedBankId(clientSecret)
                }
            delay(1.seconds)
            val context = mockk<Context>()
            every { contextProvider.invoke() } returns context
            launch {
                (onAdditionalInfoReceivedSlot.captured).provide(activityResultRegistry)
            }
            delay(1.seconds)
            launch {
                activityResultCallbackSlot.captured.onActivityResult(activityResult)
            }

            val result = job.await()
            assertIs<StripeSdkException>(result.exceptionOrNull())
            assertEquals("error", result.exceptionOrNull()?.message)
            verify {
                mockConfigurationDelegate.isMockedFlow()
                stripePublishableKeyDelegate.invoke()
                activityResultRegistry.register(
                    "bank_collector",
                    any<ActivityResultContracts.StartActivityForResult>(),
                    any(),
                )
                contextProvider.invoke()
                StripeBankAccountCollectorActivity.getLaunchIntent(
                    context = context,
                    params =
                    StripeBankAccountCollectorActivity.Params(
                        fullName = "John Doe",
                        emailAddress = "john@doe.com",
                        publishableKey = "pk",
                        clientSecret = clientSecret,
                    ),
                )
                activityResultLauncher.launch(intent)
                activityResult.resultCode
                activityResult.data
            }
            coVerify {
                getClientSessionCustomerDetailsDelegate.invoke()
            }
            confirmVerified(
                activityResultRegistry,
                activityResultLauncher,
                activityResult,
                context,
            )
            unmockkObject(StripeBankAccountCollectorActivity.Companion)
        }

    @Test
    fun `fetchSelectedBankId() should return error when bank collection flow returns unknown result code`() =
        runTest {
            every { mockConfigurationDelegate.isMockedFlow() } returns false
            every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
            val onAdditionalInfoReceivedSlot = slot<AchAdditionalInfo.ProvideActivityResultRegistry>()
            coEvery { checkoutAdditionalInfoHandler.handle(capture(onAdditionalInfoReceivedSlot)) } just Runs
            coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns
                Result.success(
                    GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                        firstName = "John",
                        lastName = "Doe",
                        emailAddress = "john@doe.com",
                    ),
                )
            val activityResultRegistry = mockk<ActivityResultRegistry>()
            val activityResultCallbackSlot = slot<ActivityResultCallback<ActivityResult>>()
            mockkObject(StripeBankAccountCollectorActivity.Companion)
            val intent = mockk<Intent>()
            every { StripeBankAccountCollectorActivity.getLaunchIntent(any(), any()) } returns intent
            val activityResultLauncher = mockk<ActivityResultLauncher<Intent>>()
            every { activityResultLauncher.launch(any()) } just Runs
            every {
                activityResultRegistry.register(
                    any(),
                    any<ActivityResultContracts.StartActivityForResult>(),
                    capture(activityResultCallbackSlot),
                )
            } returns activityResultLauncher
            val activityResult =
                mockk<ActivityResult> {
                    every { resultCode } returns 1337
                }

            val job =
                async {
                    handler.fetchSelectedBankId(clientSecret)
                }
            delay(1.seconds)
            val context = mockk<Context>()
            every { contextProvider.invoke() } returns context
            launch {
                (onAdditionalInfoReceivedSlot.captured).provide(activityResultRegistry)
            }
            delay(1.seconds)
            launch {
                activityResultCallbackSlot.captured.onActivityResult(activityResult)
            }

            val result = job.await()
            assertIs<IllegalStateException>(result.exceptionOrNull())
            assertEquals("Unsupported activity result code", result.exceptionOrNull()?.message)
            verify {
                mockConfigurationDelegate.isMockedFlow()
                stripePublishableKeyDelegate.invoke()
                activityResultRegistry.register(
                    "bank_collector",
                    any<ActivityResultContracts.StartActivityForResult>(),
                    any(),
                )
                contextProvider.invoke()
                StripeBankAccountCollectorActivity.getLaunchIntent(
                    context = context,
                    params =
                    StripeBankAccountCollectorActivity.Params(
                        fullName = "John Doe",
                        emailAddress = "john@doe.com",
                        publishableKey = "pk",
                        clientSecret = clientSecret,
                    ),
                )
                activityResultLauncher.launch(intent)
                activityResult.resultCode
            }
            coVerify {
                getClientSessionCustomerDetailsDelegate.invoke()
            }
            confirmVerified(
                activityResultRegistry,
                activityResultLauncher,
                activityResult,
                context,
            )
            unmockkObject(StripeBankAccountCollectorActivity.Companion)
        }

    @Test
    fun `fetchSelectedBankId() should not trigger the bank collection flow when mocking`() =
        runTest {
            every { mockConfigurationDelegate.isMockedFlow() } returns true
            every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
            coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns
                Result.success(
                    GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                        firstName = "John",
                        lastName = "Doe",
                        emailAddress = "john@doe.com",
                    ),
                )
            mockkObject(StripeBankAccountCollectorActivity.Companion)

            val result = handler.fetchSelectedBankId(clientSecret)

            assertEquals("mock_payment_method_id", result.getOrNull())
            verify {
                mockConfigurationDelegate.isMockedFlow()
                stripePublishableKeyDelegate.invoke()
            }
            coVerify {
                getClientSessionCustomerDetailsDelegate.invoke()
            }
            verify(exactly = 0) {
                StripeBankAccountCollectorActivity.getLaunchIntent(
                    context = any<Context>(),
                    params = any<StripeBankAccountCollectorActivity.Params>(),
                )
            }
            confirmVerified(StripeBankAccountCollectorActivity.Companion)
            unmockkObject(StripeBankAccountCollectorActivity.Companion)
        }
}
