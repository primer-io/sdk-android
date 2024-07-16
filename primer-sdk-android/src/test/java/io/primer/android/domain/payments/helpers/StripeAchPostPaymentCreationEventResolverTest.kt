package io.primer.android.domain.payments.helpers

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
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.exception.StripeIllegalValueKey.MISSING_CLIENT_SECRET
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.exception.StripeIllegalValueKey.MISSING_PUBLISHABLE_KEY
import io.primer.android.components.presentation.mock.delegate.MockConfigurationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.GetClientSessionCustomerDetailsDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.GetStripePublishableKeyDelegate
import io.primer.android.data.base.exceptions.IllegalValueException
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.data.token.model.ClientToken
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.additionalInfo.AchAdditionalInfo
import io.primer.android.domain.payments.additionalInfo.AchAdditionalInfoResolver
import io.primer.android.domain.payments.additionalInfo.AdditionalInfoResolverExtraParams
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.stripe.StripeBankAccountCollectorActivity
import io.primer.android.stripe.exceptions.StripeSdkException
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class StripeAchPostPaymentCreationEventResolverTest {
    @RelaxedMockK
    private lateinit var clientToken: ClientToken

    @MockK
    private lateinit var contextProvider: () -> Context

    @MockK
    private lateinit var eventDispatcher: EventDispatcher

    @MockK
    private lateinit var checkoutErrorEventResolver: BaseErrorEventResolver

    @MockK
    private lateinit var stripePublishableKeyDelegate: GetStripePublishableKeyDelegate

    @MockK
    private lateinit var getClientSessionCustomerDetailsDelegate:
        GetClientSessionCustomerDetailsDelegate

    @MockK
    private lateinit var mockConfigurationDelegate: MockConfigurationDelegate

    @InjectMockKs
    private lateinit var resolver: StripeAchPostPaymentCreationEventResolver

    @AfterEach
    fun tearDown() {
        confirmVerified(
            clientToken,
            contextProvider,
            eventDispatcher,
            checkoutErrorEventResolver,
            stripePublishableKeyDelegate,
            getClientSessionCustomerDetailsDelegate,
            mockConfigurationDelegate
        )
    }

    @Test
    fun `resolve() should emit error if stripeClientSecret is missing`() = runTest {
        every { mockConfigurationDelegate.isMockedFlow() } returns false
        every { clientToken.stripeClientSecret } returns null
        every { checkoutErrorEventResolver.resolve(any(), any()) } just Runs

        resolver.resolve(clientToken, mockk())

        verify {
            clientToken.stripeClientSecret
            checkoutErrorEventResolver.resolve(
                throwable = IllegalValueException(
                    key = MISSING_CLIENT_SECRET,
                    message = "Required value for ${MISSING_CLIENT_SECRET.key} was null."
                ),
                type = ErrorMapperType.STRIPE
            )
        }
    }

    @Test
    fun `resolve() should emit error if the Stripe Publishable Key delegate returns error`() = runTest {
        every { mockConfigurationDelegate.isMockedFlow() } returns false
        every { stripePublishableKeyDelegate.invoke() } returns Result.failure(Exception())
        every { checkoutErrorEventResolver.resolve(any(), any()) } just Runs

        resolver.resolve(clientToken, mockk())

        verify {
            clientToken.stripeClientSecret
            stripePublishableKeyDelegate.invoke()
            checkoutErrorEventResolver.resolve(
                throwable = IllegalValueException(
                    key = MISSING_PUBLISHABLE_KEY,
                    message = "Required value for ${MISSING_PUBLISHABLE_KEY.key} was null."
                ),
                type = ErrorMapperType.STRIPE
            )
        }
    }

    @Test
    fun `resolve() should trigger the bank collection flow`() = runTest {
        every { mockConfigurationDelegate.isMockedFlow() } returns false
        every { clientToken.stripeClientSecret } returns "stripeClientSecret"
        val onBankSelected = mockk<(AdditionalInfoResolverExtraParams) -> Unit> params@{
            every { this@params.invoke(any()) } just Runs
        }
        every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
        val onAdditionalInfoReceivedSlot = slot<CheckoutEvent.OnAdditionalInfoReceived>()
        every { eventDispatcher.dispatchEvent(capture(onAdditionalInfoReceivedSlot)) } just Runs
        coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns Result.success(
            GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                firstName = "John",
                lastName = "Doe",
                emailAddress = "john@doe.com"
            )
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
                capture(activityResultCallbackSlot)
            )
        } returns activityResultLauncher
        val activityResult = mockk<ActivityResult> {
            every { resultCode } returns Activity.RESULT_OK
            every { data?.extras?.getString(any()) } returns "paymentMethodId"
        }

        val resolveJob = launch {
            resolver.resolve(clientToken, onBankSelected)
        }
        delay(1.seconds)
        val context = mockk<Context>()
        every { contextProvider.invoke() } returns context
        launch {
            (
                onAdditionalInfoReceivedSlot.captured.paymentMethodInfo
                    as AchAdditionalInfo.ProvideActivityResultRegistry
                ).provide(
                activityResultRegistry
            )
        }
        delay(1.seconds)
        launch {
            activityResultCallbackSlot.captured.onActivityResult(activityResult)
        }
        resolveJob.join()

        verify {
            mockConfigurationDelegate.isMockedFlow()
            clientToken.stripeClientSecret
            stripePublishableKeyDelegate.invoke()
            eventDispatcher.dispatchEvent(any())
            activityResultRegistry.register(
                "bank_collector",
                any<ActivityResultContracts.StartActivityForResult>(),
                any()
            )
            contextProvider.invoke()
            StripeBankAccountCollectorActivity.getLaunchIntent(
                context = context,
                params = StripeBankAccountCollectorActivity.Params(
                    fullName = "John Doe",
                    emailAddress = "john@doe.com",
                    publishableKey = "pk",
                    clientSecret = "stripeClientSecret"
                )
            )
            activityResultLauncher.launch(intent)
            activityResult.resultCode
            activityResult.data?.extras?.getString(
                StripeBankAccountCollectorActivity.PAYMENT_METHOD_ID
            )
            onBankSelected.invoke(
                AchAdditionalInfoResolver.AchAdditionalInfoResolverExtraParams(
                    "paymentMethodId"
                )
            )
        }
        coVerify {
            getClientSessionCustomerDetailsDelegate.invoke()
        }
        confirmVerified(
            onBankSelected,
            activityResultRegistry,
            activityResultLauncher,
            activityResult,
            context
        )
        unmockkObject(StripeBankAccountCollectorActivity.Companion)
    }

    @Test
    fun `resolve() should emit error when bank collection flow is cancelled`() = runTest {
        every { mockConfigurationDelegate.isMockedFlow() } returns false
        every { clientToken.stripeClientSecret } returns "stripeClientSecret"
        val onBankSelected = mockk<(AdditionalInfoResolverExtraParams) -> Unit> params@{
            every { this@params.invoke(any()) } just Runs
        }
        every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
        val onAdditionalInfoReceivedSlot = slot<CheckoutEvent.OnAdditionalInfoReceived>()
        every { eventDispatcher.dispatchEvent(capture(onAdditionalInfoReceivedSlot)) } just Runs
        coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns Result.success(
            GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                firstName = "John",
                lastName = "Doe",
                emailAddress = "john@doe.com"
            )
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
                capture(activityResultCallbackSlot)
            )
        } returns activityResultLauncher
        val activityResult = mockk<ActivityResult> {
            every { resultCode } returns Activity.RESULT_CANCELED
        }
        every { checkoutErrorEventResolver.resolve(any(), any()) } just Runs

        val resolveJob = launch {
            resolver.resolve(clientToken, onBankSelected)
        }
        delay(1.seconds)
        val context = mockk<Context>()
        every { contextProvider.invoke() } returns context
        launch {
            (
                onAdditionalInfoReceivedSlot.captured.paymentMethodInfo
                    as AchAdditionalInfo.ProvideActivityResultRegistry
                ).provide(
                activityResultRegistry
            )
        }
        delay(1.seconds)
        launch {
            activityResultCallbackSlot.captured.onActivityResult(activityResult)
        }
        resolveJob.join()

        verify {
            mockConfigurationDelegate.isMockedFlow()
            clientToken.stripeClientSecret
            stripePublishableKeyDelegate.invoke()
            eventDispatcher.dispatchEvent(any())
            activityResultRegistry.register(
                "bank_collector",
                any<ActivityResultContracts.StartActivityForResult>(),
                any()
            )
            contextProvider.invoke()
            StripeBankAccountCollectorActivity.getLaunchIntent(
                context = context,
                params = StripeBankAccountCollectorActivity.Params(
                    fullName = "John Doe",
                    emailAddress = "john@doe.com",
                    publishableKey = "pk",
                    clientSecret = "stripeClientSecret"
                )
            )
            activityResultLauncher.launch(intent)
            activityResult.resultCode
            checkoutErrorEventResolver.resolve(
                PaymentMethodCancelledException(paymentMethodType = "STRIPE_ACH"),
                ErrorMapperType.STRIPE
            )
        }
        coVerify {
            getClientSessionCustomerDetailsDelegate.invoke()
        }
        confirmVerified(
            onBankSelected,
            activityResultRegistry,
            activityResultLauncher,
            activityResult,
            context,
            checkoutErrorEventResolver
        )
        unmockkObject(StripeBankAccountCollectorActivity.Companion)
    }

    @Test
    fun `resolve() should emit error when bank collection flow errors`() = runTest {
        every { mockConfigurationDelegate.isMockedFlow() } returns false
        every { clientToken.stripeClientSecret } returns "stripeClientSecret"
        val onBankSelected = mockk<(AdditionalInfoResolverExtraParams) -> Unit> params@{
            every { this@params.invoke(any()) } just Runs
        }
        every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
        val onAdditionalInfoReceivedSlot = slot<CheckoutEvent.OnAdditionalInfoReceived>()
        every { eventDispatcher.dispatchEvent(capture(onAdditionalInfoReceivedSlot)) } just Runs
        coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns Result.success(
            GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                firstName = "John",
                lastName = "Doe",
                emailAddress = "john@doe.com"
            )
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
                capture(activityResultCallbackSlot)
            )
        } returns activityResultLauncher
        val exception = Exception()
        val activityResult = mockk<ActivityResult> {
            every { resultCode } returns StripeBankAccountCollectorActivity.RESULT_ERROR
            every { data?.extras?.getSerializable(any()) } returns exception
        }
        every { checkoutErrorEventResolver.resolve(any(), any()) } just Runs

        val resolveJob = launch {
            resolver.resolve(clientToken, onBankSelected)
        }
        delay(1.seconds)
        val context = mockk<Context>()
        every { contextProvider.invoke() } returns context
        launch {
            (
                onAdditionalInfoReceivedSlot.captured.paymentMethodInfo
                    as AchAdditionalInfo.ProvideActivityResultRegistry
                ).provide(
                activityResultRegistry
            )
        }
        delay(1.seconds)
        launch {
            activityResultCallbackSlot.captured.onActivityResult(activityResult)
        }
        resolveJob.join()

        verify {
            mockConfigurationDelegate.isMockedFlow()
            clientToken.stripeClientSecret
            stripePublishableKeyDelegate.invoke()
            eventDispatcher.dispatchEvent(any())
            activityResultRegistry.register(
                "bank_collector",
                any<ActivityResultContracts.StartActivityForResult>(),
                any()
            )
            contextProvider.invoke()
            StripeBankAccountCollectorActivity.getLaunchIntent(
                context = context,
                params = StripeBankAccountCollectorActivity.Params(
                    fullName = "John Doe",
                    emailAddress = "john@doe.com",
                    publishableKey = "pk",
                    clientSecret = "stripeClientSecret"
                )
            )
            activityResultLauncher.launch(intent)
            activityResult.resultCode
            activityResult.data
            checkoutErrorEventResolver.resolve(
                exception,
                ErrorMapperType.STRIPE
            )
        }
        coVerify {
            getClientSessionCustomerDetailsDelegate.invoke()
        }
        confirmVerified(
            onBankSelected,
            activityResultRegistry,
            activityResultLauncher,
            activityResult,
            context,
            checkoutErrorEventResolver
        )
        unmockkObject(StripeBankAccountCollectorActivity.Companion)
    }

    @Test
    fun `resolve() should emit error when bank collection flow errors but exception is null`() = runTest {
        every { mockConfigurationDelegate.isMockedFlow() } returns false
        every { clientToken.stripeClientSecret } returns "stripeClientSecret"
        val onBankSelected = mockk<(AdditionalInfoResolverExtraParams) -> Unit> params@{
            every { this@params.invoke(any()) } just Runs
        }
        every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
        val onAdditionalInfoReceivedSlot = slot<CheckoutEvent.OnAdditionalInfoReceived>()
        every { eventDispatcher.dispatchEvent(capture(onAdditionalInfoReceivedSlot)) } just Runs
        coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns Result.success(
            GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                firstName = "John",
                lastName = "Doe",
                emailAddress = "john@doe.com"
            )
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
                capture(activityResultCallbackSlot)
            )
        } returns activityResultLauncher
        val activityResult = mockk<ActivityResult> {
            every { resultCode } returns StripeBankAccountCollectorActivity.RESULT_ERROR
            every { data?.extras?.getSerializable(any()) } returns null
        }
        every { checkoutErrorEventResolver.resolve(any(), any()) } just Runs

        val resolveJob = launch {
            resolver.resolve(clientToken, onBankSelected)
        }
        delay(1.seconds)
        val context = mockk<Context>()
        every { contextProvider.invoke() } returns context
        launch {
            (
                onAdditionalInfoReceivedSlot.captured.paymentMethodInfo
                    as AchAdditionalInfo.ProvideActivityResultRegistry
                ).provide(
                activityResultRegistry
            )
        }
        delay(1.seconds)
        launch {
            activityResultCallbackSlot.captured.onActivityResult(activityResult)
        }
        resolveJob.join()

        verify {
            mockConfigurationDelegate.isMockedFlow()
            clientToken.stripeClientSecret
            stripePublishableKeyDelegate.invoke()
            eventDispatcher.dispatchEvent(any())
            activityResultRegistry.register(
                "bank_collector",
                any<ActivityResultContracts.StartActivityForResult>(),
                any()
            )
            contextProvider.invoke()
            StripeBankAccountCollectorActivity.getLaunchIntent(
                context = context,
                params = StripeBankAccountCollectorActivity.Params(
                    fullName = "John Doe",
                    emailAddress = "john@doe.com",
                    publishableKey = "pk",
                    clientSecret = "stripeClientSecret"
                )
            )
            activityResultLauncher.launch(intent)
            activityResult.resultCode
            activityResult.data
            checkoutErrorEventResolver.resolve(
                withArg {
                    assertIs<StripeSdkException>(it)
                    assertEquals("error", it.message)
                },
                ErrorMapperType.STRIPE
            )
        }
        coVerify {
            getClientSessionCustomerDetailsDelegate.invoke()
        }
        confirmVerified(
            onBankSelected,
            activityResultRegistry,
            activityResultLauncher,
            activityResult,
            context,
            checkoutErrorEventResolver
        )
        unmockkObject(StripeBankAccountCollectorActivity.Companion)
    }

    @Test
    fun `resolve() should emit error when bank collection returns unknown result code`() = runTest {
        every { mockConfigurationDelegate.isMockedFlow() } returns false
        every { clientToken.stripeClientSecret } returns "stripeClientSecret"
        val onBankSelected = mockk<(AdditionalInfoResolverExtraParams) -> Unit> params@{
            every { this@params.invoke(any()) } just Runs
        }
        every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
        val onAdditionalInfoReceivedSlot = slot<CheckoutEvent.OnAdditionalInfoReceived>()
        every { eventDispatcher.dispatchEvent(capture(onAdditionalInfoReceivedSlot)) } just Runs
        coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns Result.success(
            GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                firstName = "John",
                lastName = "Doe",
                emailAddress = "john@doe.com"
            )
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
                capture(activityResultCallbackSlot)
            )
        } returns activityResultLauncher
        val activityResult = mockk<ActivityResult> {
            every { resultCode } returns 1337
        }
        every { checkoutErrorEventResolver.resolve(any(), any()) } just Runs

        val resolveJob = launch {
            resolver.resolve(clientToken, onBankSelected)
        }
        delay(1.seconds)
        val context = mockk<Context>()
        every { contextProvider.invoke() } returns context
        launch {
            (
                onAdditionalInfoReceivedSlot.captured.paymentMethodInfo
                    as AchAdditionalInfo.ProvideActivityResultRegistry
                ).provide(
                activityResultRegistry
            )
        }
        delay(1.seconds)
        launch {
            activityResultCallbackSlot.captured.onActivityResult(activityResult)
        }
        resolveJob.join()

        verify {
            mockConfigurationDelegate.isMockedFlow()
            clientToken.stripeClientSecret
            stripePublishableKeyDelegate.invoke()
            eventDispatcher.dispatchEvent(any())
            activityResultRegistry.register(
                "bank_collector",
                any<ActivityResultContracts.StartActivityForResult>(),
                any()
            )
            contextProvider.invoke()
            StripeBankAccountCollectorActivity.getLaunchIntent(
                context = context,
                params = StripeBankAccountCollectorActivity.Params(
                    fullName = "John Doe",
                    emailAddress = "john@doe.com",
                    publishableKey = "pk",
                    clientSecret = "stripeClientSecret"
                )
            )
            activityResultLauncher.launch(intent)
            activityResult.resultCode
            checkoutErrorEventResolver.resolve(
                withArg {
                    assertIs<IllegalStateException>(it)
                    assertEquals("Unsupported activity result code", it.message)
                },
                ErrorMapperType.STRIPE
            )
        }
        coVerify {
            getClientSessionCustomerDetailsDelegate.invoke()
        }
        confirmVerified(
            onBankSelected,
            activityResultRegistry,
            activityResultLauncher,
            activityResult,
            context,
            checkoutErrorEventResolver
        )
        unmockkObject(StripeBankAccountCollectorActivity.Companion)
    }

    @Test
    fun `resolve() should not trigger the bank collection flow when mocking`() = runTest {
        every { mockConfigurationDelegate.isMockedFlow() } returns true
        every { clientToken.stripeClientSecret } returns "stripeClientSecret"
        val onBankSelected = mockk<(AdditionalInfoResolverExtraParams) -> Unit> params@{
            every { this@params.invoke(any()) } just Runs
        }
        every { stripePublishableKeyDelegate.invoke() } returns Result.success("pk")
        mockkObject(StripeBankAccountCollectorActivity.Companion)

        resolver.resolve(clientToken, onBankSelected)

        verify {
            mockConfigurationDelegate.isMockedFlow()
            clientToken.stripeClientSecret
            stripePublishableKeyDelegate.invoke()
            onBankSelected.invoke(
                AchAdditionalInfoResolver.AchAdditionalInfoResolverExtraParams(
                    "mock_payment_method_id"
                )
            )
        }
        verify(exactly = 0) {
            StripeBankAccountCollectorActivity.getLaunchIntent(
                context = any<Context>(),
                params = any<StripeBankAccountCollectorActivity.Params>()
            )
        }
        confirmVerified(StripeBankAccountCollectorActivity.Companion, onBankSelected)
        unmockkObject(StripeBankAccountCollectorActivity.Companion)
    }
}
