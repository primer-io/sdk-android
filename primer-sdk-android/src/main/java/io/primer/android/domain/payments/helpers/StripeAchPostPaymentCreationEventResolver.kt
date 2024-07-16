package io.primer.android.domain.payments.helpers

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.exception.StripeIllegalValueKey
import io.primer.android.components.presentation.mock.delegate.MockConfigurationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.GetClientSessionCustomerDetailsDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.GetStripePublishableKeyDelegate
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.data.token.model.ClientToken
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.additionalInfo.AchAdditionalInfo
import io.primer.android.domain.payments.additionalInfo.AchAdditionalInfoResolver.AchAdditionalInfoResolverExtraParams
import io.primer.android.domain.payments.additionalInfo.AdditionalInfoResolverExtraParams
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.flatMap
import io.primer.android.stripe.StripeBankAccountCollectorActivity
import io.primer.android.stripe.exceptions.StripeSdkException
import io.primer.android.utils.getSerializableExtraCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

private val MOCK_COLLECTION_DELAY = 2.seconds

internal class StripeAchPostPaymentCreationEventResolver(
    private val contextProvider: () -> Context,
    private val eventDispatcher: EventDispatcher,
    private val checkoutErrorEventResolver: BaseErrorEventResolver,
    private val stripePublishableKeyDelegate: GetStripePublishableKeyDelegate,
    private val getClientSessionCustomerDetailsDelegate: GetClientSessionCustomerDetailsDelegate,
    private val mockConfigurationDelegate: MockConfigurationDelegate
) {
    private lateinit var clientSecret: String
    private lateinit var publishableKey: String
    private val isMockedFlow get() = mockConfigurationDelegate.isMockedFlow()

    @Suppress("Detekt.TooGenericExceptionCaught")
    suspend fun resolve(
        clientToken: ClientToken,
        onBankSelected: (AdditionalInfoResolverExtraParams) -> Unit
    ) {
        try {
            clientSecret = requireNotNullCheck(
                value = clientToken.stripeClientSecret,
                key = StripeIllegalValueKey.MISSING_CLIENT_SECRET
            )
            publishableKey = requireNotNullCheck(
                value = stripePublishableKeyDelegate().getOrNull(),
                key = StripeIllegalValueKey.MISSING_PUBLISHABLE_KEY
            )
        } catch (throwable: Throwable) {
            dispatchCheckoutError(throwable)
            return
        }

        collectStripeBank()
            .onSuccess {
                onBankSelected(AchAdditionalInfoResolverExtraParams(paymentMethodId = it))
            }
            .onFailure(::dispatchCheckoutError)
    }

    // region Utils
    @Suppress("ReturnCount")
    private suspend fun collectStripeBank(): Result<String> {
        if (isMockedFlow) {
            delay(MOCK_COLLECTION_DELAY)
            return Result.success("mock_payment_method_id")
        }

        val activityResultRegistry = getActivityResultRegistry()

        return getClientSessionCustomerDetailsDelegate.invoke().flatMap {
            return suspendCancellableCoroutine<Result<String>> { continuation ->
                activityResultRegistry.register(
                    /* key = */ "bank_collector",
                    /* contract = */ ActivityResultContracts.StartActivityForResult()
                ) { activityResult ->
                    if (!continuation.isActive) {
                        return@register
                    }
                    continuation.resume(
                        when (activityResult.resultCode) {
                            Activity.RESULT_OK -> Result.success(
                                activityResult.data?.extras?.getString(
                                    StripeBankAccountCollectorActivity.PAYMENT_METHOD_ID
                                ).orEmpty()
                            )

                            Activity.RESULT_CANCELED -> Result.failure(
                                PaymentMethodCancelledException(
                                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name
                                )
                            )

                            StripeBankAccountCollectorActivity.RESULT_ERROR -> {
                                Result.failure(
                                    activityResult.data?.extras?.getSerializableExtraCompat(
                                        StripeBankAccountCollectorActivity.ERROR_KEY
                                    ) ?: StripeSdkException("error")
                                )
                            }

                            else -> Result.failure(
                                IllegalStateException("Unsupported activity result code")
                            )
                        }
                    )
                }.launch(
                    StripeBankAccountCollectorActivity.getLaunchIntent(
                        context = contextProvider(),
                        params = StripeBankAccountCollectorActivity.Params(
                            fullName = "${it.firstName} ${it.lastName}",
                            emailAddress = it.emailAddress,
                            publishableKey = publishableKey,
                            clientSecret = clientSecret
                        )
                    )
                )
            }
        }
    }

    private fun dispatchCheckoutError(throwable: Throwable) {
        checkoutErrorEventResolver.resolve(
            throwable = throwable,
            type = ErrorMapperType.STRIPE
        )
    }
    // endregion

    // region Event emitters
    private suspend fun getActivityResultRegistry(): ActivityResultRegistry =
        suspendCancellableCoroutine { continuation ->
            eventDispatcher.dispatchEvent(
                CheckoutEvent.OnAdditionalInfoReceived(
                    paymentMethodInfo = AchAdditionalInfo.ProvideActivityResultRegistry(
                        provide = {
                            if (continuation.isActive) {
                                continuation.resume(it)
                            }
                        }
                    )
                )
            )
        }
    // endregion
}
