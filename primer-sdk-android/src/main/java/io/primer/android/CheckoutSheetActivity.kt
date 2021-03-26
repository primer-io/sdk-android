package io.primer.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import io.primer.android.di.DIAppContext
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.Model
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.CheckoutExitInfo
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.model.json
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.WebBrowserIntentBehaviour
import io.primer.android.payment.paypal.PayPal
import io.primer.android.ui.fragments.*
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.ViewStatus
import kotlinx.serialization.serializer
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension

// TODO manual di with this
typealias ActivityViewModelFactoryProvider = (Fragment) -> ViewModelProvider.Factory

// TODO manual di with this
val activityViewModelFactoryProvider: ActivityViewModelFactoryProvider = {
    it.requireActivity().defaultViewModelProviderFactory
}

// TODO manual di with this
internal inline fun <reified VM : ViewModel> Fragment.viewModelBuilder(
    useActivityStore: Boolean = false,
    noinline viewModelInitializer: () -> VM,
): Lazy<VM> {
    return ViewModelLazy(
        viewModelClass = VM::class,
        storeProducer = {
            if (useActivityStore) requireActivity().viewModelStore else viewModelStore
        },
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return viewModelInitializer.invoke() as T
                }
            }
        }
    )
}

// TODO manual di with this
interface ViewModelAssistedFactory<T : ViewModel> {

    fun create(handle: SavedStateHandle): T
}

// TODO manual di with this
internal class PrimerViewModelFactory(
    private val model: Model,
    private val checkoutConfig: CheckoutConfig,
    private val configuredPaymentMethods: List<PaymentMethod>,
) : ViewModelAssistedFactory<PrimerViewModel> {

    override fun create(handle: SavedStateHandle): PrimerViewModel {
        // return PrimerViewModel(model, checkoutConfig, configuredPaymentMethods)
        return PrimerViewModel()
    }
}

// TODO manual di with this
class GenericSavedStateViewModelFactory<out V : ViewModel>(
    private val viewModelFactory: ViewModelAssistedFactory<V>,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null,
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return viewModelFactory.create(handle) as T
    }
}

@KoinApiExtension
internal class CheckoutSheetActivity : AppCompatActivity() {

    private var subscription: EventBus.SubscriptionHandle? = null
    private var exited = false
    private var initFinished = false

    private val mainViewModel: PrimerViewModel by viewModels()
    // private lateinit var viewModelFactory: PrimerViewModelFactory

    private val tokenizationViewModel: TokenizationViewModel by viewModels()

    private lateinit var sheet: CheckoutSheetFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val checkoutConfig = unmarshal<CheckoutConfig>("config")
        val paymentMethods = unmarshal<List<PaymentMethod>>("paymentMethods")

        DIAppContext.init(this, checkoutConfig, paymentMethods)

        // TODO manual di
        // val clientToken = ClientToken.fromString(checkoutConfig.clientToken)
        // val apiClient = APIClient(clientToken)
        // val model = Model(apiClient, clientToken, checkoutConfig)
        // viewModelFactory = PrimerViewModelFactory(model, checkoutConfig, paymentMethods)

        mainViewModel.initialize()

        sheet = CheckoutSheetFragment.newInstance()

        attachViewModelListeners()

        attachEventListeners()

        openSheet()
    }

    private inline fun <reified T> unmarshal(name: String): T {
        // FIXME this should be parcelized instead
        val serialized = intent.getStringExtra(name)
        return json.decodeFromString(serializer(), serialized!!) // TODO avoid !!
    }

    override fun onResume() {
        super.onResume()
        WebviewInteropRegister.invokeAll()
    }

    private fun attachViewModelListeners() {
        mainViewModel.viewStatus.observe(
            this,
            {
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
            }
        )

        mainViewModel.selectedPaymentMethod.observe(
            this,
            { paymentMethod: PaymentMethodDescriptor? ->
                paymentMethod?.let {
                    when (val behaviour = it.selectedBehaviour) {
                        is NewFragmentBehaviour -> {
                            openFragment(behaviour)
                        }
                        is WebBrowserIntentBehaviour -> {
                            behaviour.execute(tokenizationViewModel)
                        }
                        else -> {
                            // TODO what should we do here?
                        }
                    }
                }
            }
        )

        tokenizationViewModel.payPalBillingAgreementUrl.observe(this) { uri: String ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }

        tokenizationViewModel.confirmPayPalBillingAgreement.observe(this) { data: JSONObject ->
            val paymentMethod: PaymentMethodDescriptor? = mainViewModel.selectedPaymentMethod.value
            val paypal = paymentMethod as? PayPal ?: return@observe // if we are getting an emission here it means we're currently dealing with paypal

            paypal.setTokenizableValue(
                "paypalBillingAgreementId",
                data.getString("billingAgreementId")
            )
            paypal.setTokenizableValue("externalPayerInfo", data.getJSONObject("externalPayerInfo"))
            paypal.setTokenizableValue("shippingAddress", data.getJSONObject("shippingAddress"))

            tokenizationViewModel.tokenize()
        }

        tokenizationViewModel.payPalOrder.observe(this) { uri: String ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }
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
