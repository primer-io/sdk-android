package io.primer.android.threeds.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import io.primer.android.BaseCheckoutActivity
import io.primer.android.R
import io.primer.android.di.DIAppContext
import io.primer.android.threeds.di.threeDsModule
import io.primer.android.threeds.domain.models.ChallengeStatusData.Companion.TRANSACTION_STATUS_FAILURE
import io.primer.android.threeds.domain.models.ChallengeStatusData.Companion.TRANSACTION_STATUS_SUCCESS
import io.primer.android.threeds.presentation.ThreeDsViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class ThreeDsActivity : BaseCheckoutActivity() {

    private val viewModel: ThreeDsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DIAppContext.app?.modules(threeDsModule)
        setContentView(R.layout.activity_primer_progress)
        setupViews()
        setupObservers()
        viewModel.startThreeDsFlow()
    }

    private fun setupViews() {
        // we need to add this due to security reason
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
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
        DIAppContext.app?.unloadModules(threeDsModule)
    }

    companion object {

        fun getLaunchIntent(context: Context) =
            Intent(context, ThreeDsActivity::class.java)
    }
}
