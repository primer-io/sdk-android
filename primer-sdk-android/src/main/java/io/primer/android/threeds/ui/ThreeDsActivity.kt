package io.primer.android.threeds.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import io.primer.android.BaseCheckoutActivity
import io.primer.android.R
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.di.DISdkContext
import io.primer.android.di.extension.viewModel
import io.primer.android.threeds.di.ThreeDsContainer
import io.primer.android.threeds.presentation.ThreeDsViewModel
import io.primer.android.threeds.presentation.ThreeDsViewModelFactory

internal class ThreeDsActivity : BaseCheckoutActivity() {

    private val viewModel: ThreeDsViewModel
        by viewModel<ThreeDsViewModel, ThreeDsViewModelFactory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_primer_progress)
        DISdkContext.sdkContainer?.let { it.registerContainer(ThreeDsContainer(it)) }
        setupViews()
        setupObservers()
        if (savedInstanceState == null) {
            logAnalyticsViewed()
            viewModel.startThreeDsFlow()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun setupViews() {
        // we need to add this due to security reason
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun setupObservers() {
        viewModel.threeDsInitEvent.observe(this) {
            viewModel.performAuthorization()
        }

        viewModel.challengeRequiredEvent.observe(this) { challengeRequiredData ->
            viewModel.performChallenge(
                this,
                challengeRequiredData.transaction,
                challengeRequiredData.authData
            )
        }

        viewModel.threeDsStatusChangedEvent.observe(this) {
            viewModel.continueRemoteAuth(it)
        }

        viewModel.threeDsErrorEvent.observe(this) {
            viewModel.continueRemoteAuthWithException(it)
        }

        viewModel.threeDsFinishedEvent.observe(this) {
            finish()
        }
    }

    private fun logAnalyticsViewed() = viewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.VIEW,
            ObjectType.VIEW,
            Place.`3DS_VIEW`
        )
    )

    override fun onDestroy() {
        super.onDestroy()
        DISdkContext.sdkContainer?.unregisterContainer<ThreeDsContainer>()
    }

    companion object {

        fun getLaunchIntent(context: Context) =
            Intent(context, ThreeDsActivity::class.java)
    }
}
