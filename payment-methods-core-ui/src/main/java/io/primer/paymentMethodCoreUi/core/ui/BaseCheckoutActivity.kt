package io.primer.paymentMethodCoreUi.core.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.extensions.inject
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.paymentMethodCoreUi.core.ui.extension.withLocale

open class BaseCheckoutActivity : AppCompatActivity(), DISdkComponent {
    protected val logReporter by inject<LogReporter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
        logReporter.debug("Creating activity (hashcode ${hashCode()})")
        supportActionBar?.hide()

        val deeplinkLaunchAfterKill: Boolean = savedInstanceState == null && intent?.data != null
        val processRestartWithoutContainer: Boolean =
            savedInstanceState != null && DISdkContext.isHeadlessInitialized.not()

        if (deeplinkLaunchAfterKill || processRestartWithoutContainer) {
            logReporter.warn(
                "Finishing activity (hashcode ${hashCode()}) because headless container is null or empty",
            )
            finish()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val locale = DISdkContext.integrationContext.locale
        val wrapped = locale.let { newBase?.withLocale(it) } ?: newBase
        super.attachBaseContext(wrapped)
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

    /**
     * Finishes the activity when the headless SDK has no initialized dependency container, which is the case
     * when `cleanup()` ran while this activity was being launched. Returns true when the activity is finishing.
     */
    protected fun finishIfSdkIsNotInitialized(): Boolean {
        if (DISdkContext.isHeadlessInitialized) return false
        logReporter.warn("Finishing activity (hashcode ${hashCode()}) because the headless SDK is not initialized")
        finish()
        return true
    }

    companion object {
        const val PRIMER_CONFIG_KEY = "PRIMER_CONFIG"
        private const val SAVED_STATE_CONFIG_KEY = "CONFIG"
    }
}
