package io.primer.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import io.primer.android.ui.fragments.*
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationStatus
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

    private val viewStatusObserver = Observer<ViewStatus> {
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

    private val selectPaymentMethodObserver = Observer<PaymentMethodDescriptor?> {
        it?.let {
            when (val behaviour = it.selectedBehaviour) {
                is NewFragmentBehaviour -> {
                    openFragment(behaviour)
                }
                is WebBrowserIntentBehaviour -> {
                    behaviour.execute(tokenizationViewModel)
                }
                is WebViewBehaviour -> {
                    // this calls viewModel.createKlarnaBillingAgreement(id, returnUrl)
                    // which ultimately posts Triple(hppRedirectUrl, klarnaReturnUrl, sessionId) to klarnaPaymentData
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
        mainViewModel.selectedPaymentMethod.observe(this, selectPaymentMethodObserver)

        // region klarna
        tokenizationViewModel.klarnaPaymentData.observe(this) { (paymentUrl, redirectUrl) ->
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra(WebViewActivity.PAYMENT_URL_KEY, paymentUrl)
                putExtra(WebViewActivity.CAPTURE_URL_KEY, redirectUrl)
            }
            startActivityForResult(intent, KLARNA_REQUEST_CODE)
        }

        tokenizationViewModel.finalizeKlarnaPayment.observe(this) { data: JSONObject ->
            val paymentMethod: PaymentMethodDescriptor? = mainViewModel.selectedPaymentMethod.value
            val klarna = paymentMethod as? Klarna
                ?: return@observe // if we are getting an emission here it means we're currently dealing with klarna

            klarna.setTokenizableValue("klarnaAuthorizationToken", data.optString("token"))
            klarna.setTokenizableValue("sessionData", data.getJSONObject("sessionData"))

            tokenizationViewModel.tokenize()
        }

        tokenizationViewModel.tokenizationStatus.observe(this) { status ->
            val paymentMethod: PaymentMethodDescriptor? = mainViewModel.selectedPaymentMethod.value
            if (paymentMethod !is Klarna || status != TokenizationStatus.SUCCESS) return@observe

            if (checkoutConfig.uxMode == UXMode.ADD_PAYMENT_METHOD) {
                tokenizationViewModel.finalizeKlarnaPayment.value?.let { data ->
                    val id = paymentMethod.config.id ?: return@observe
                    val token = data.optString("token")
                    tokenizationViewModel.saveKlarnaPayment(id, token)
                }
            }
        }
        // endregion

        // region paypal
        tokenizationViewModel.payPalBillingAgreementUrl.observe(this) { uri: String ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }

        tokenizationViewModel.confirmPayPalBillingAgreement.observe(this) { data: JSONObject ->
            val paymentMethod: PaymentMethodDescriptor? = mainViewModel.selectedPaymentMethod.value
            val paypal = paymentMethod as? PayPal
                ?: return@observe // if we are getting an emission here it means we're currently dealing with paypal

            paypal.setTokenizableValue("paypalBillingAgreementId", data.getString("billingAgreementId"))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KLARNA_REQUEST_CODE -> handleKlarnaRequestResult(resultCode, data)
            else -> {
                // unexpected request code
            }
        }
    }

    private fun handleKlarnaRequestResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                val redirectUrl = data?.extras?.getString(WebViewActivity.REDIRECT_URL_KEY)
                val uri = Uri.parse(redirectUrl)
                val token = uri.getQueryParameter("token")

                val paymentMethod: PaymentMethodDescriptor? = mainViewModel.selectedPaymentMethod.value
                val klarna = paymentMethod as? Klarna

                if (redirectUrl == null || klarna == null || token == null) {
                    // TODO error
                    return
                }
                val id = klarna.config.id ?: return
                tokenizationViewModel.finalizeKlarnaPayment(id, token)
            }
            RESULT_CANCELED -> {
                // TODO
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
        sheet.show(supportFragmentManager, sheet.tag)
    }
}
