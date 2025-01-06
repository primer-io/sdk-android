@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.stripe.ach.implementation.selection.presentation

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import io.primer.android.configuration.mock.presentation.MockConfigurationDelegate
import io.primer.android.core.extensions.getSerializableExtraCompat
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.stripe.StripeBankAccountCollectorActivity
import io.primer.android.stripe.ach.api.additionalInfo.AchAdditionalInfo
import io.primer.android.stripe.ach.implementation.session.data.exception.StripeIllegalValueKey
import io.primer.android.stripe.ach.implementation.session.presentation.GetClientSessionCustomerDetailsDelegate
import io.primer.android.stripe.ach.implementation.session.presentation.GetStripePublishableKeyDelegate
import io.primer.android.stripe.exceptions.StripeSdkException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

private val MOCK_COLLECTION_DELAY = 2.seconds

internal class StripeAchBankSelectionHandler(
    private val contextProvider: () -> Context,
    private val checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
    private val stripePublishableKeyDelegate: GetStripePublishableKeyDelegate,
    private val getClientSessionCustomerDetailsDelegate: GetClientSessionCustomerDetailsDelegate,
    private val mockConfigurationDelegate: MockConfigurationDelegate,
) {
    private val isMockedFlow get() = mockConfigurationDelegate.isMockedFlow()

    suspend fun fetchSelectedBankId(clientSecret: String): Result<String> {
        lateinit var fullName: String
        lateinit var emailAddress: String
        lateinit var publishableKey: String

        try {
            publishableKey =
                requireNotNullCheck(
                    value = stripePublishableKeyDelegate().getOrNull(),
                    key = StripeIllegalValueKey.MISSING_PUBLISHABLE_KEY,
                )
            val customerDetails = getClientSessionCustomerDetailsDelegate.invoke().getOrThrow()
            fullName = "${customerDetails.firstName} ${customerDetails.lastName}"
            emailAddress = customerDetails.emailAddress
        } catch (throwable: Throwable) {
            return Result.failure(throwable)
        }

        return fetchSelectedBankId(
            fullName = fullName,
            emailAddress = emailAddress,
            clientSecret = clientSecret,
            publishableKey = publishableKey,
        )
    }

    // region Utils
    @Suppress("ReturnCount")
    private suspend fun fetchSelectedBankId(
        fullName: String,
        emailAddress: String,
        clientSecret: String,
        publishableKey: String,
    ): Result<String> {
        if (isMockedFlow) {
            delay(MOCK_COLLECTION_DELAY)
            return Result.success("mock_payment_method_id")
        }

        val activityResultRegistry = getActivityResultRegistry()

        return suspendCancellableCoroutine<Result<String>> { continuation ->
            activityResultRegistry.register(
                // key =
                "bank_collector",
                // contract =
                ActivityResultContracts.StartActivityForResult(),
            ) { activityResult ->
                if (!continuation.isActive) {
                    return@register
                }
                continuation.resume(
                    when (activityResult.resultCode) {
                        Activity.RESULT_OK ->
                            Result.success(
                                activityResult.data?.extras?.getString(
                                    StripeBankAccountCollectorActivity.PAYMENT_METHOD_ID,
                                ).orEmpty(),
                            )

                        Activity.RESULT_CANCELED ->
                            Result.failure(
                                PaymentMethodCancelledException(
                                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                                ),
                            )

                        StripeBankAccountCollectorActivity.RESULT_ERROR -> {
                            Result.failure(
                                activityResult.data?.extras?.getSerializableExtraCompat(
                                    StripeBankAccountCollectorActivity.ERROR_KEY,
                                ) ?: StripeSdkException("error"),
                            )
                        }

                        else ->
                            Result.failure(
                                IllegalStateException("Unsupported activity result code"),
                            )
                    },
                )
            }.launch(
                StripeBankAccountCollectorActivity.getLaunchIntent(
                    context = contextProvider(),
                    params =
                        StripeBankAccountCollectorActivity.Params(
                            fullName = fullName,
                            emailAddress = emailAddress,
                            publishableKey = publishableKey,
                            clientSecret = clientSecret,
                        ),
                ),
            )
        }
    }
    // endregion

    // region Event emitters
    private suspend fun getActivityResultRegistry(): ActivityResultRegistry {
        val completable = CompletableDeferred<ActivityResultRegistry>()
        checkoutAdditionalInfoHandler.handle(
            AchAdditionalInfo.ProvideActivityResultRegistry(
                provide = { activityResultRegistry ->
                    if (completable.isActive) {
                        completable.complete(activityResultRegistry)
                    }
                },
            ),
        )
        return completable.await()
    }
    // endregion
}
