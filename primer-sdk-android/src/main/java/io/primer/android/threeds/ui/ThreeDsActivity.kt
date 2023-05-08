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
import io.primer.android.di.DIAppContext
import io.primer.android.threeds.di.threeDsModule
import io.primer.android.threeds.domain.models.ChallengeStatusData.Companion.TRANSACTION_STATUS_FAILURE
import io.primer.android.threeds.domain.models.ChallengeStatusData.Companion.TRANSACTION_STATUS_SUCCESS
import io.primer.android.threeds.presentation.ThreeDsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class ThreeDsActivity : BaseCheckoutActivity() {

    private val viewModel: ThreeDsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DIAppContext.app?.modules(threeDsModule)
        setContentView(R.layout.activity_primer_progress)
        setupViews()
        setupObservers()
        logAnalyticsViewed()
        viewModel.startThreeDsFlow()
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

        viewModel.challengeStatusChangedEvent.observe(this) {
            when (it.transactionStatus) {
                TRANSACTION_STATUS_SUCCESS -> viewModel.continueRemoteAuth(it.sdkTransId)
                TRANSACTION_STATUS_FAILURE -> finish()
            }
        }

        viewModel.threeDsFinishedEvent.observe(this) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        getKoin().unloadModules(listOf(threeDsModule))
    }

    private fun logAnalyticsViewed() = viewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.VIEW,
            ObjectType.VIEW,
            Place.`3DS_VIEW`,
        )
    )

    companion object {

        fun getLaunchIntent(context: Context) =
            Intent(context, ThreeDsActivity::class.java)
    }
}
