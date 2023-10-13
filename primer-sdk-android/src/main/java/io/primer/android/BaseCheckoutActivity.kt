package io.primer.android

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.di.DISdkComponent
import io.primer.android.di.DISdkContext
import io.primer.android.di.extension.resolve

internal open class BaseCheckoutActivity : AppCompatActivity(), DISdkComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        if (DISdkContext.sdkContainer == null) {
            // we need to restore the state in case of process death
            val config = getConfigFromState(savedInstanceState)
            config?.let {
                DISdkContext.init(config, applicationContext)
                // TODO we need to take care of re-fetching of the configuration,
                //  after that it will be possible to restore state.
                finish()
            } ?: run {
                // if we can't restore it, gracefully finish.
                // This can happen in case when screen is launched using deeplink,
                // but user closed app manually.
                finish()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SAVED_STATE_CONFIG_KEY, resolve<PrimerConfig>())
    }

    private fun getConfigFromState(savedInstanceState: Bundle?) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState?.getParcelable(SAVED_STATE_CONFIG_KEY, PrimerConfig::class.java)
        } else { savedInstanceState?.getParcelable(SAVED_STATE_CONFIG_KEY) as? PrimerConfig }

    private companion object {
        const val SAVED_STATE_CONFIG_KEY = "CONFIG"
    }
}
