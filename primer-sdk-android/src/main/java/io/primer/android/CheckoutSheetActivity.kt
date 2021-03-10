package io.primer.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.primer.android.di.DIAppContext
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.Logger
import io.primer.android.model.dto.CheckoutExitInfo
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.model.json
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.WebBrowserIntentBehaviour
import io.primer.android.ui.fragments.*
import io.primer.android.viewmodel.BaseViewModel
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.ViewStatus
import kotlinx.serialization.serializer
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class CheckoutSheetActivity : AppCompatActivity() {

    private val log = Logger("checkout-activity")
    private var subscription: EventBus.SubscriptionHandle? = null
    private var exited = false
    private lateinit var viewModel: PrimerViewModel
    private lateinit var tokenizationViewModel: TokenizationViewModel
    private lateinit var sheet: CheckoutSheetFragment
    private var initFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DIAppContext.init(
            this,
            config = unmarshal("config"),
            paymentMethods = unmarshal("paymentMethods"),
        )

        viewModel = initializeViewModel(PrimerViewModel::class.java)
        tokenizationViewModel = initializeViewModel(TokenizationViewModel::class.java)

        sheet = CheckoutSheetFragment.newInstance()

        attachViewModelListeners()

        attachEventListeners()

        openSheet()
    }

    private inline fun <reified T> unmarshal(name: String): T {
        val serialized = intent.getStringExtra(name)
        return json.decodeFromString(serializer(), serialized!!)
    }

    private fun <T : BaseViewModel> initializeViewModel(modelCls: Class<T>, ): T {
        val vm = ViewModelProvider(this).get(modelCls)
        vm.initialize()
        return vm
    }

    override fun onResume() {
        super.onResume()
        WebviewInteropRegister.invokeAll()
    }

    private fun attachViewModelListeners() {
        viewModel.viewStatus.observe(this, {
            val fragment = when (it) {
                ViewStatus.INITIALIZING -> InitializingFragment.newInstance()
                ViewStatus.SELECT_PAYMENT_METHOD -> SelectPaymentMethodFragment.newInstance()
                ViewStatus.VIEW_VAULTED_PAYMENT_METHODS -> VaultedPaymentMethodsFragment.newInstance()
                else -> null
            }

            if (fragment != null) {
                openFragment(fragment, initFinished)
            }

            if (!initFinished && it != ViewStatus.INITIALIZING) {
                initFinished = true
            }
        })

        viewModel.selectedPaymentMethod.observe(this, { pm ->
            pm?.let {
                when (val behaviour = it.selectedBehaviour) {
                    is NewFragmentBehaviour -> openFragment(behaviour)
                    is WebBrowserIntentBehaviour -> behaviour.execute(this, tokenizationViewModel)
                    else -> {
                    }
                }
            }
        })
    }

    private fun attachEventListeners() {
        subscription = EventBus.subscribe {
            when (it) {
                is CheckoutEvent.DismissInternal -> {
                    onExit(it.data)
                }
                is CheckoutEvent.ShowSuccess -> {
                    openFragment(SuccessFragment.newInstance(it.delay))
                }
                is CheckoutEvent.ToggleProgressIndicator -> {
                    onToggleProgressIndicator(it.data)
                }
            }
        }
    }

    private fun onExit(reason: CheckoutExitReason) {
        if (!exited) {
            exited = true
            EventBus.broadcast(CheckoutEvent.Exit(CheckoutExitInfo(reason)))
            finish()
        }
    }

    private fun openFragment(fragment: Fragment, returnToPreviousOnBack: Boolean = false) {
        openFragment(NewFragmentBehaviour({ fragment }, returnToPreviousOnBack))
    }

    private fun openFragment(behaviour: NewFragmentBehaviour) {
        behaviour.execute(sheet)
    }

    private fun onToggleProgressIndicator(visible: Boolean) {
        if (visible) {
            openFragment(ProgressIndicatorFragment.newInstance(), true)
        } else {
            sheet.childFragmentManager.popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription?.unregister()
        subscription = null
    }

    private fun openSheet() {
        supportFragmentManager.let {
            sheet.apply {
                show(it, tag)
            }
        }
    }
}
