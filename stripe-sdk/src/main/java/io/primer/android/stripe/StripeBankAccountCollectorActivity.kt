package io.primer.android.stripe

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.stripe.android.PaymentConfiguration
import com.stripe.android.core.exception.InvalidRequestException
import com.stripe.android.model.StripeIntent
import com.stripe.android.payments.bankaccount.CollectBankAccountConfiguration
import com.stripe.android.payments.bankaccount.CollectBankAccountLauncher
import com.stripe.android.payments.bankaccount.navigation.CollectBankAccountResult
import io.primer.android.stripe.exceptions.StripePublishableKeyMismatchException
import io.primer.android.stripe.exceptions.StripeSdkException
import io.primer.android.stripe.utils.getSerializableExtraCompat
import java.io.Serializable

class StripeBankAccountCollectorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        disableEnterAnimations()
        setupOnBackPressDispatcher()

        val params = requireParams()

        PaymentConfiguration.init(
            context = applicationContext,
            publishableKey = params.publishableKey
        )

        val collectBankAccountLauncher =
            CollectBankAccountLauncher.create(
                activity = this,
                callback = createBankAccountResultCallback()
            )

        collectBankAccountLauncher.presentWithPaymentIntent(
            publishableKey = params.publishableKey,
            clientSecret = params.clientSecret,
            configuration =
                CollectBankAccountConfiguration.USBankAccount(
                    name = params.fullName,
                    email = params.emailAddress
                )
        )
    }

    private fun setupOnBackPressDispatcher() {
        onBackPressedDispatcher.addCallback(owner = this) {
            setCancellationResult()
            finish()
        }
    }

    override fun finish() {
        disableExitAnimations()
        super.finish()
    }

    private fun createBankAccountResultCallback() =
        { result: CollectBankAccountResult ->
            when (result) {
                is CollectBankAccountResult.Completed -> {
                    val resultIntent = result.response.intent
                    if (resultIntent.status === StripeIntent.Status.RequiresPaymentMethod) {
                        setCancellationResult()
                    } else if (resultIntent.status === StripeIntent.Status.RequiresConfirmation) {
                        setResult(
                            RESULT_OK,
                            Intent().apply { putExtra(PAYMENT_METHOD_ID, result.response.intent.paymentMethodId) }
                        )
                    } else {
                        // no-op
                    }
                }

                is CollectBankAccountResult.Cancelled -> setCancellationResult()

                is CollectBankAccountResult.Failed -> {
                    val stripeError = (result.error as? InvalidRequestException)?.stripeError
                    setResult(
                        RESULT_ERROR,
                        Intent().apply {
                            putExtra(
                                ERROR_KEY,
                                if (stripeError?.code == "resource_missing") {
                                    StripePublishableKeyMismatchException(stripeError.message)
                                } else {
                                    StripeSdkException(result.error.message)
                                }
                            )
                        }
                    )
                }
            }
            finish()
        }

    private fun setCancellationResult() {
        setResult(RESULT_CANCELED, Intent())
    }

    // region Utils
    private fun requireParams() =
        requireNotNull(
            intent.extras?.getSerializableExtraCompat<Params>(PARAMS_KEY)
        )

    private fun disableEnterAnimations() {
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                0,
                0,
                Color.TRANSPARENT
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    private fun disableExitAnimations() {
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                0,
                0,
                Color.TRANSPARENT
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
    // endregion

    data class Params(
        val fullName: String,
        val emailAddress: String,
        val publishableKey: String,
        val clientSecret: String
    ) : Serializable

    companion object {
        const val RESULT_ERROR = RESULT_FIRST_USER + 1

        // Input
        private const val PARAMS_KEY = "PARAMS"

        // Output
        const val ERROR_KEY = "ERROR"
        const val PAYMENT_METHOD_ID = "PAYMENT_METHOD_ID"

        @JvmStatic
        fun getLaunchIntent(
            context: Context,
            params: Params
        ): Intent =
            Intent(
                context,
                StripeBankAccountCollectorActivity::class.java
            ).apply {
                putExtra(PARAMS_KEY, params)
            }
    }
}
