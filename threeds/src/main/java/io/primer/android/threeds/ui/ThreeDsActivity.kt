package io.primer.android.threeds.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.core.di.extensions.registerContainer
import io.primer.android.core.di.extensions.unregisterContainer
import io.primer.android.core.di.extensions.viewModel
import io.primer.android.core.extensions.getSerializableCompat
import io.primer.android.threeds.di.ThreeDsContainer
import io.primer.android.threeds.main.R
import io.primer.android.threeds.presentation.ThreeDsViewModel
import io.primer.android.threeds.presentation.ThreeDsViewModelFactory
import io.primer.android.threeds.ui.launcher.ThreeDsActivityLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.BaseCheckoutActivity

class ThreeDsActivity : BaseCheckoutActivity() {
    private val viewModel: ThreeDsViewModel
        by viewModel<ThreeDsViewModel, ThreeDsViewModelFactory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_primer_progress)
        runIfNotFinishing {
            registerContainer(containerProvider = { ThreeDsContainer(it) })
            setupViews()
            setupObservers()
            if (savedInstanceState == null) {
                logAnalyticsViewed()
                viewModel.startThreeDsFlow()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    @Deprecated("Deprecated in Java")
    @Suppress("MissingSuperCall")
    override fun onBackPressed() {
        // disable back button
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterContainer<ThreeDsContainer>()
    }

    private fun setupViews() {
        // we need to add this due to security reason
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun setupObservers() {
        viewModel.threeDsInitEvent.observe(this) {
            viewModel.performAuthorization(supportedThreeDsProtocolVersions = getSupportedThreeDsProtocolVersion())
        }

        viewModel.challengeRequiredEvent.observe(this) { challengeRequiredData ->
            viewModel.performChallenge(
                activity = this,
                transaction = challengeRequiredData.transaction,
                authData = challengeRequiredData.authData,
            )
        }

        viewModel.threeDsStatusChangedEvent.observe(this) { challengeStatusData ->
            viewModel.continueRemoteAuth(
                challengeStatusData = challengeStatusData,
                supportedThreeDsProtocolVersions = getSupportedThreeDsProtocolVersion(),
            )
        }

        viewModel.threeDsErrorEvent.observe(this) { throwable ->
            viewModel.continueRemoteAuthWithException(
                throwable = throwable,
                supportedThreeDsProtocolVersions = getSupportedThreeDsProtocolVersion(),
            )
        }

        viewModel.threeDsFinishedEvent.observe(this) { resumeToken ->
            setResult(
                RESULT_OK,
                Intent().apply {
                    putExtra(RESUME_TOKEN_EXTRA_KEY, resumeToken)
                },
            )
            finish()
        }
    }

    private fun logAnalyticsViewed() =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.VIEW,
                ObjectType.VIEW,
                Place.`3DS_VIEW`,
            ),
        )

    private fun getSupportedThreeDsProtocolVersion() =
        intent.getSerializableCompat<ThreeDsActivityLauncherParams>(
            INTENT_PARAMS_EXTRA_KEY,
        )?.supportedThreeDsProtocolVersions.orEmpty()

    companion object {
        const val INTENT_PARAMS_EXTRA_KEY = "INTENT_PARAMS_EXTRA"

        const val RESUME_TOKEN_EXTRA_KEY = "RESUME_TOKEN"

        fun getLaunchIntent(
            context: Context,
            params: ThreeDsActivityLauncherParams,
        ): Intent {
            return Intent(context, ThreeDsActivity::class.java).apply {
                putExtra(INTENT_PARAMS_EXTRA_KEY, params)
            }
        }
    }
}
