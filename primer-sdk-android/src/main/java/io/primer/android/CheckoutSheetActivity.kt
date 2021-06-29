package io.primer.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.wallet.PaymentData
import io.primer.android.WebViewActivity.Companion.RESULT_ERROR
import io.primer.android.di.DIAppComponent
import io.primer.android.di.DIAppContext
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.KlarnaPaymentData
import io.primer.android.model.Model
import io.primer.android.model.Serialization
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.CheckoutExitInfo
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.payment.KLARNA_IDENTIFIER
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.payment.WebBrowserIntentBehaviour
import io.primer.android.payment.WebViewBehaviour
import io.primer.android.payment.google.GooglePayDescriptor
import io.primer.android.payment.google.GooglePayDescriptor.Companion.GOOGLE_PAY_REQUEST_CODE
import io.primer.android.payment.google.InitialCheckRequiredBehaviour
import io.primer.android.payment.klarna.KlarnaDescriptor
import io.primer.android.payment.klarna.KlarnaDescriptor.Companion.KLARNA_REQUEST_CODE
import io.primer.android.payment.paypal.PayPalDescriptor
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.ui.fragments.InitializingFragment
import io.primer.android.ui.fragments.ProgressIndicatorFragment
import io.primer.android.ui.fragments.SelectPaymentMethodFragment
import io.primer.android.ui.fragments.SessionCompleteFragment
import io.primer.android.ui.fragments.SessionCompleteViewType
import io.primer.android.ui.fragments.VaultedPaymentMethodsFragment
import io.primer.android.viewmodel.GenericSavedStateAndroidViewModelFactory
import io.primer.android.viewmodel.PrimerPaymentMethodCheckerRegistry
import io.primer.android.viewmodel.PrimerPaymentMethodDescriptorResolver
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.PrimerViewModelFactory
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.ViewStatus
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class CheckoutSheetActivity : AppCompatActivity(), DIAppComponent {

    private var subscription: EventBus.SubscriptionHandle? = null
    private var exited = false
    private var initFinished = false

    private lateinit var viewModelFactory: PrimerViewModelFactory
    private val primerViewModel: PrimerViewModel by viewModels {
        GenericSavedStateAndroidViewModelFactory(application, viewModelFactory, this)
    }

    private val model: Model by inject() // FIXME manual di here

    private val tokenizationViewModel: TokenizationViewModel by viewModels()

    private lateinit var sheet: CheckoutSheetFragment
    private lateinit var checkoutConfig: CheckoutConfig

    private val viewStatusObserver = Observer<ViewStatus> {
        if (checkoutConfig.doNotShowUi) {
            return@Observer
        }

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

    // region KLARNA-related observers
    private val klarnaPaymentDataObserver =
        Observer<KlarnaPaymentData> { (paymentUrl, redirectUrl) ->
            if (checkoutConfig.preferWebView) {

                val paymentMethod = primerViewModel.selectedPaymentMethod.value

                val title: String = if (paymentMethod is KlarnaDescriptor) {
                    paymentMethod.options.webViewTitle
                } else {
                    ""
                }

                // TODO  a klarna flow that is not recurring requires this:
                val intent = Intent(this, WebViewActivity::class.java).apply {
                    putExtra(WebViewActivity.PAYMENT_URL_KEY, paymentUrl)
                    putExtra(WebViewActivity.CAPTURE_URL_KEY, redirectUrl)
                    putExtra(WebViewActivity.TOOLBAR_TITLE_KEY, title)
                }

                startActivityForResult(intent, KLARNA_REQUEST_CODE)
            } else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
                startActivity(intent)
            }
        }

    private val klarnaVaultedObserver = Observer<JSONObject> { data ->
        val paymentMethod: PaymentMethodDescriptor? =
            primerViewModel.selectedPaymentMethod.value

        // if we are getting an emission here it means we should currently be dealing with klarna
        val klarna = paymentMethod as? KlarnaDescriptor ?: return@Observer

        klarna.setTokenizableValue(
            "klarnaCustomerToken",
            data.optString("customerTokenId")
        )
        klarna.setTokenizableValue("sessionData", data.getJSONObject("sessionData"))

        tokenizationViewModel.tokenize()
    }
    // endregion

    // region PAYPAL-related observers
    private val confirmPayPalBillingAgreementObserver = Observer<JSONObject> { data: JSONObject ->
        val paymentMethod: PaymentMethodDescriptor? =
            primerViewModel.selectedPaymentMethod.value

        // if we are getting an emission here it means we should currently be dealing with paypal
        val paypal = paymentMethod as? PayPalDescriptor ?: return@Observer

        paypal.setTokenizableValue(
            "paypalBillingAgreementId",
            data.getString("billingAgreementId")
        )
        paypal.setTokenizableValue("externalPayerInfo", data.getJSONObject("externalPayerInfo"))
        paypal.setTokenizableValue("shippingAddress", data.getJSONObject("shippingAddress"))

        tokenizationViewModel.tokenize()
    }

    private val payPalBillingAgreementUrlObserver = Observer { uri: String ->
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
    }

    private val payPalOrderObserver = Observer { uri: String ->
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
    }
    // endregion

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
                is InitialCheckRequiredBehaviour -> {
                    behaviour.execute(this, tokenizationViewModel)
                }
                else -> {
                    // TODO what should we do here?
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val json = Serialization.json

        val checkoutConfig = intent
            .getStringExtra("config")
            ?.let { json.decodeFromString(CheckoutConfig.serializer(), it) } ?: return

        val locallyConfiguredPaymentMethods = intent
            .getStringExtra("paymentMethods")
            ?.let { json.decodeFromString<List<PaymentMethod>>(it) } ?: return

        if (checkoutConfig.doNotShowUi) {
            ensureClicksGoThrough()
        }

        this.checkoutConfig = checkoutConfig

        DIAppContext.init(this, checkoutConfig, locallyConfiguredPaymentMethods)

        val paymentMethodRegistry = PrimerPaymentMethodCheckerRegistry
        val paymentMethodDescriptorFactoryRegistry =
            PaymentMethodDescriptorFactoryRegistry(paymentMethodRegistry)

        val paymentMethodDescriptorResolver = PrimerPaymentMethodDescriptorResolver(
            localConfig = checkoutConfig,
            localPaymentMethods = locallyConfiguredPaymentMethods,
            paymentMethodDescriptorFactoryRegistry = paymentMethodDescriptorFactoryRegistry,
            availabilityCheckers = paymentMethodRegistry
        )
        viewModelFactory = PrimerViewModelFactory(
            model = model,
            checkoutConfig = checkoutConfig,
            paymentMethodCheckerRegistry = paymentMethodRegistry,
            paymentMethodDescriptorFactoryRegistry = paymentMethodDescriptorFactoryRegistry,
            primerPaymentMethodDescriptorResolver = paymentMethodDescriptorResolver
        )

        primerViewModel.fetchConfiguration(locallyConfiguredPaymentMethods)

        sheet = CheckoutSheetFragment.newInstance()

        primerViewModel.viewStatus.observe(this, viewStatusObserver)
        primerViewModel.selectedPaymentMethod.observe(this, selectedPaymentMethodObserver)

        tokenizationViewModel.tokenizationCanceled.observe(this) {
            onExit(CheckoutExitReason.DISMISSED_BY_USER)
        }

        // region KLARNA
        tokenizationViewModel.klarnaPaymentData.observe(this, klarnaPaymentDataObserver)
        tokenizationViewModel.vaultedKlarnaPayment.observe(this, klarnaVaultedObserver)
        tokenizationViewModel.klarnaError.observe(this) {
            val apiError = APIError("Failed to add Klarna payment method.")
            EventBus.broadcast(CheckoutEvent.ApiError(apiError))
        }
        // endregion

        // region PAYPAL
        tokenizationViewModel.payPalBillingAgreementUrl.observe(
            this,
            payPalBillingAgreementUrlObserver
        )
        tokenizationViewModel.confirmPayPalBillingAgreement.observe(
            this,
            confirmPayPalBillingAgreementObserver
        )
        tokenizationViewModel.payPalOrder.observe(this, payPalOrderObserver)
        // endregion

        subscription = EventBus.subscribe {
            when (it) {
                is CheckoutEvent.DismissInternal -> {
                    onExit(it.data)
                }
                is CheckoutEvent.ShowSuccess -> {
                    openFragment(
                        SessionCompleteFragment.newInstance(
                            it.delay,
                            SessionCompleteViewType.Success(it.successType),
                        )
                    )
                }
                is CheckoutEvent.ShowError -> {
                    openFragment(
                        SessionCompleteFragment.newInstance(
                            it.delay,
                            SessionCompleteViewType.Error(it.errorType),
                        )
                    )
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
        window
            .addFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
    }

    override fun onResume() {
        super.onResume()
        if (!checkoutConfig.preferWebView ||
            primerViewModel.selectedPaymentMethod.value?.identifier != KLARNA_IDENTIFIER
        ) {
            WebviewInteropRegister.invokeAll()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KLARNA_REQUEST_CODE -> handleKlarnaRequestResult(resultCode, data)
            GOOGLE_PAY_REQUEST_CODE -> handleGooglePayRequestResult(resultCode, data)
            else -> Unit
        }
    }

    private fun handleKlarnaRequestResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                val redirectUrl = data?.data.toString()
                val paymentMethod = primerViewModel.selectedPaymentMethod.value
                val klarna = paymentMethod as? KlarnaDescriptor

                tokenizationViewModel.handleKlarnaRequestResult(klarna, redirectUrl)
            }
            RESULT_ERROR -> {
                onExit(CheckoutExitReason.ERROR)
            }
            RESULT_CANCELED -> {
                onExit(CheckoutExitReason.DISMISSED_BY_USER)
            }
        }
    }

    private fun handleGooglePayRequestResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                val paymentMethod = primerViewModel.selectedPaymentMethod.value
                val googlePay = paymentMethod as? GooglePayDescriptor
                data?.let {
                    val paymentData = PaymentData.getFromIntent(data)
                    tokenizationViewModel.handleGooglePayRequestResult(paymentData, googlePay)
                }
            }
            RESULT_CANCELED -> {
                // TODO check if this behavior is correct/right
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
        if (checkoutConfig.doNotShowUi) {
            return
        }

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

private inline fun <reified T> Intent.unmarshal(
    name: String,
    json: Json,
): T? {
    val serialized = getStringExtra(name)
    return serialized?.let { json.decodeFromString(serializer(), it) }
}
