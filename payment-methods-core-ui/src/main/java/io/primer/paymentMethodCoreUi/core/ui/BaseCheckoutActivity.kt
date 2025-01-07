package io.primer.paymentMethodCoreUi.core.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.extensions.inject
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.settings.internal.PrimerConfig

open class BaseCheckoutActivity : AppCompatActivity(), DISdkComponent {
    protected val logReporter by inject<LogReporter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logReporter.debug("Creating activity (hashcode ${hashCode()})")
        supportActionBar?.hide()

        if (savedInstanceState != null && DISdkContext.headlessSdkContainer?.containers.isNullOrEmpty()) {
            logReporter.warn(
                "Finishing activity (hashcode ${hashCode()}) because headless container is null or empty",
            )
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SAVED_STATE_CONFIG_KEY, runCatching { resolve<PrimerConfig>() }.getOrNull())
    }

    protected fun AppCompatActivity.runIfNotFinishing(block: () -> Unit) {
        if (isFinishing.not()) {
            block()
        }
    }

    private companion object {
        const val SAVED_STATE_CONFIG_KEY = "CONFIG"
    }
}
