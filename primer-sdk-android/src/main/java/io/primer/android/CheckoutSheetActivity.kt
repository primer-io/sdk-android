package io.primer.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import io.primer.android.WebViewActivity.Companion.RESULT_ERROR
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
import io.primer.android.payment.WebViewBehaviour
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.klarna.Klarna.Companion.KLARNA_REQUEST_CODE
import io.primer.android.payment.paypal.PayPal
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.ui.fragments.InitializingFragment
import io.primer.android.ui.fragments.ProgressIndicatorFragment
import io.primer.android.ui.fragments.SelectPaymentMethodFragment
import io.primer.android.ui.fragments.SuccessFragment
import io.primer.android.ui.fragments.VaultedPaymentMethodsFragment
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

    // private lateinit var viewModelFactory: PrimerViewModelFactory
    private val mainViewModel: PrimerViewModel by viewModels()
    private val tokenizationViewModel: TokenizationViewModel by viewModels()

    private lateinit var sheet: CheckoutSheetFragment
    private lateinit var checkoutConfig: CheckoutConfig

    private val viewStatusObserver = Observer<ViewStatus> {
        if (checkoutConfig.doNotShowUi) {
            return@Observer
        };

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

    private val selectedPaymentMethodObserver = Observer<PaymentMethodDescriptor?> {
        it?.let {
            when (val behaviour = it.selectedBehaviour) {
                is NewFragmentBehaviour -> {
                    openFragment(behaviour)
                }
                is WebBrowserIntentBehaviour -> {
                    behaviour.execute(tokenizationViewModel)
                }
                is WebViewBehaviour -> {
                    behaviour.execute(tokenizationViewModel)
                }
                else -> {
                    // TODO what should we do here?
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val checkoutConfig = unmarshal<CheckoutConfig>("config")
        val paymentMethods = unmarshal<List<PaymentMethod>>("paymentMethods")

        if (checkoutConfig.doNotShowUi) {
            ensureClicksGoThrough()
        }

        this.checkoutConfig = checkoutConfig

        DIAppContext.init(this, checkoutConfig, paymentMethods)

        /* TODO manual di
        val clientToken = ClientToken.fromString(checkoutConfig.clientToken)
        val apiClient = APIClient(clientToken)
        val model = Model(apiClient, clientToken, checkoutConfig)
        viewModelFactory = PrimerViewModelFactory(model, checkoutConfig, paymentMethods)
        */

        mainViewModel.initialize()

        sheet = CheckoutSheetFragment.newInstance()

        mainViewModel.viewStatus.observe(this, viewStatusObserver)
        mainViewModel.selectedPaymentMethod.observe(this, selectedPaymentMethodObserver)

        tokenizationViewModel.tokenizationCanceled.observe(this) {
            onExit(CheckoutExitReason.DISMISSED_BY_USER)
        }

        // region KLARNA
        tokenizationViewModel.klarnaPaymentData.observe(this) { (paymentUrl, redirectUrl) ->
            // TODO  a klarna flow that is not recurring requires this:
            // val intent = Intent(this, WebViewActivity::class.java).apply {
            //     putExtra(WebViewActivity.PAYMENT_URL_KEY, paymentUrl)
            //     putExtra(WebViewActivity.CAPTURE_URL_KEY, redirectUrl)
            // }
            // startActivity(intent)

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
            startActivityForResult(intent, KLARNA_REQUEST_CODE)
        }

        tokenizationViewModel.vaultedKlarnaPayment.observe(this) { data ->
            val paymentMethod: PaymentMethodDescriptor? = mainViewModel.selectedPaymentMethod.value
            val klarna = paymentMethod as? Klarna
                ?: return@observe // if we are getting an emission here it means we're currently dealing with klarna

            klarna.setTokenizableValue(
                "klarnaCustomerToken",
                data.optString("customerTokenId")
            )
            klarna.setTokenizableValue("sessionData", data.getJSONObject("sessionData"))

            tokenizationViewModel.tokenize()
        }

        tokenizationViewModel.klarnaError.observe(this) {
            // TODO
        }
        // endregion

        // region PAYPAL
        tokenizationViewModel.payPalBillingAgreementUrl.observe(this) { uri: String ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }

        tokenizationViewModel.confirmPayPalBillingAgreement.observe(this) { data: JSONObject ->
            val paymentMethod: PaymentMethodDescriptor? = mainViewModel.selectedPaymentMethod.value
            val paypal = paymentMethod as? PayPal
                ?: return@observe // if we are getting an emission here it means we're currently dealing with paypal

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
        // endregion

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

        if (!checkoutConfig.doNotShowUi) {
            openSheet()
        }
    }

    private fun ensureClicksGoThrough() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KLARNA_REQUEST_CODE -> {
                // TODO  a klarna flow that is not recurring will need this
                // handleKlarnaRequestResult(resultCode, data)
            }
            else -> {
                // TODO error: unexpected request code
            }
        }
    }

    private fun handleKlarnaRequestResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                val redirectUrl = data?.extras?.getString(WebViewActivity.REDIRECT_URL_KEY)
                val paymentMethod: PaymentMethodDescriptor? =
                    mainViewModel.selectedPaymentMethod.value
                val klarna = paymentMethod as? Klarna

                tokenizationViewModel.handleKlarnaRequestResult(redirectUrl, klarna)
            }
            RESULT_ERROR -> {
                onExit(CheckoutExitReason.ERROR)
            }
            RESULT_CANCELED -> {
                onExit(CheckoutExitReason.DISMISSED_BY_USER)
            }
        }
    }

    private fun onExit(reason: CheckoutExitReason) {
        if (!exited) {
            exited = true
            EventBus.broadcast(
                CheckoutEvent.Exit(CheckoutExitInfo(reason))
            )
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
        sheet.show(supportFragmentManager, sheet.tag)
    }
}
