package io.primer.ipay88.api.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ipay.IPayIH
import io.primer.ipay88.api.exceptions.IPayConnectionErrorException
import io.primer.ipay88.api.exceptions.IPayPaymentCancelledException
import io.primer.ipay88.api.exceptions.IPayPaymentFailedException
import io.primer.ipay88.internal.IPayPaymentState
import io.primer.ipay88.internal.IPayStateHolder
import io.primer.ipay88.internal.PrimerIPay88Delegate
import io.primer.ipay88.internal.extension.toIPayIHPayment

class NativeIPay88Activity : AppCompatActivity() {

    private val iPayIH by lazy { IPayIH.getInstance() }

    private val launcherIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        when (activityResult.resultCode) {
            RESULT_CANCELED -> {
                setCancelledResult(IPayPaymentCancelledException())
                finish()
            }
            RESULT_OK -> {
                when (val state = IPayStateHolder.currentState) {
                    is IPayPaymentState.Cancelled -> {
                        setCancelledResult(IPayPaymentCancelledException())
                        finish()
                    }
                    is IPayPaymentState.ConnectionError -> {
                        setErrorResult(IPayConnectionErrorException())
                        finish()
                    }
                    is IPayPaymentState.Failed -> {
                        setErrorResult(
                            IPayPaymentFailedException(
                                state.transId,
                                state.tokenId,
                                state.refNo,
                                state.errorDescription
                            )
                        )
                        finish()
                    }
                    is IPayPaymentState.Success -> {
                        setResult(RESULT_OK)
                        finish()
                    }
                    else -> {
                        setErrorResult(IllegalStateException())
                        finish()
                    }
                }
            }
            else -> finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            when {
                // in case of a deeplink, we will rely on webhooks to provide us the result
                intent.data != null -> {
                    setResult(RESULT_OK)
                    finish()
                }
                getLauncherParams() != null -> startCheckout(getLauncherParams()!!)
                else -> {
                    setCancelledResult(IPayPaymentCancelledException())
                    finish()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // in case of a deeplink, we will rely on webhooks to provide us the result.
        intent?.data?.let {
            launcherIntent.unregister()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun startCheckout(params: IPay88LauncherParams) {
        launcherIntent.launch(
            iPayIH.checkout(
                params.toIPayIHPayment(),
                this,
                PrimerIPay88Delegate(),
                params.iPayMethod
            )
        )
    }

    private fun setErrorResult(exception: IllegalStateException) {
        val errorCode = getLauncherParams()!!.errorCode
        setResult(errorCode, Intent().apply { putExtra(ERROR_KEY, exception) })
    }

    private fun setCancelledResult(exception: IllegalStateException) {
        setResult(RESULT_CANCELED, Intent().apply { putExtra(ERROR_KEY, exception) })
    }

    private fun getLauncherParams() =
        intent.getSerializableExtra(INTENT_PARAMS_EXTRA_KEY) as? IPay88LauncherParams

    companion object {

        private const val INTENT_PARAMS_EXTRA_KEY = "INTENT_PARAMS_EXTRA"
        const val ERROR_KEY = "ERROR"

        fun getLaunchIntent(context: Context, params: IPay88LauncherParams) =
            Intent(context, NativeIPay88Activity::class.java).apply {
                putExtra(INTENT_PARAMS_EXTRA_KEY, params)
            }
    }
}
