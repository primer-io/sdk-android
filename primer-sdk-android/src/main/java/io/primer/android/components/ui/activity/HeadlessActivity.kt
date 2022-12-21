package io.primer.android.components.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import io.primer.android.BaseCheckoutActivity
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.async.exception.AsyncFlowIgnoredCancellationException
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.ui.base.webview.WebViewActivity
import io.primer.ipay88.api.ui.IPay88LauncherParams
import io.primer.ipay88.api.ui.NativeIPay88Activity
import org.koin.core.component.inject

internal class HeadlessActivity : BaseCheckoutActivity() {

    private val errorEventResolver: BaseErrorEventResolver by inject()
    private val eventDispatcher: EventDispatcher by inject()

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> Unit
                WebViewActivity.RESULT_ERROR -> {
                    val exception =
                        result.data?.extras?.getSerializable(NativeIPay88Activity.ERROR_KEY)
                            as Exception
                    errorEventResolver.resolve(
                        exception,
                        ErrorMapperType.I_PAY88
                    )
                    eventDispatcher.dispatchEvent(
                        CheckoutEvent.AsyncFlowCancelled(
                            AsyncFlowIgnoredCancellationException()
                        )
                    )
                }
                RESULT_CANCELED -> {
                    eventDispatcher.dispatchEvent(CheckoutEvent.AsyncFlowCancelled())
                }
            }
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val params = getLauncherParams()
        // we don't want to start again in case of config change
        if (savedInstanceState == null) {
            resultLauncher.launch(NativeIPay88Activity.getLaunchIntent(this, params))
        }
    }

    private fun getLauncherParams() =
        intent.getSerializableExtra(PARAMS_KEY) as IPay88LauncherParams

    internal companion object {

        private const val PARAMS_KEY = "LAUNCHER_PARAMS"

        fun getLaunchIntent(
            context: Context,
            params: IPay88LauncherParams
        ): Intent {
            return Intent(context, HeadlessActivity::class.java).putExtra(
                PARAMS_KEY, params
            )
        }
    }
}
