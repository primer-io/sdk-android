package io.primer.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.wallet.PaymentData
import io.primer.android.data.action.models.ClientSessionActionsRequest
import io.primer.android.di.DIAppComponent
import io.primer.android.di.DIAppContext
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.BaseWebFlowPaymentData
import io.primer.android.model.Serialization
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.CheckoutExitInfo
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.WebBrowserIntentBehaviour
import io.primer.android.payment.WebViewBehaviour
import io.primer.android.payment.apaya.ApayaDescriptor
import io.primer.android.payment.apaya.ApayaDescriptor.Companion.APAYA_REQUEST_CODE
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor.Companion.ASYNC_METHOD_REQUEST_CODE
import io.primer.android.payment.google.GooglePayDescriptor
import io.primer.android.payment.google.GooglePayDescriptor.Companion.GOOGLE_PAY_REQUEST_CODE
import io.primer.android.payment.google.InitialCheckRequiredBehaviour
import io.primer.android.payment.klarna.KlarnaDescriptor
import io.primer.android.payment.klarna.KlarnaDescriptor.Companion.KLARNA_REQUEST_CODE
import io.primer.android.payment.paypal.PayPalDescriptor
import io.primer.android.ui.base.webview.WebViewActivity
import io.primer.android.ui.base.webview.WebViewActivity.Companion.RESULT_ERROR
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.android.ui.base.webview.WebViewClientType
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.ui.fragments.InitializingFragment
import io.primer.android.ui.fragments.ProgressIndicatorFragment
import io.primer.android.ui.fragments.SelectPaymentMethodFragment
import io.primer.android.ui.fragments.SessionCompleteFragment
import io.primer.android.ui.fragments.SessionCompleteViewType
import io.primer.android.ui.fragments.VaultedPaymentMethodsFragment
import io.primer.android.ui.fragments.forms.QrCodeFragment
import io.primer.android.ui.payment.async.AsyncPaymentMethodWebViewActivity
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.ViewStatus
import org.json.JSONObject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class CheckoutSheetActivity : AppCompatActivity(), DIAppComponent {

    private var subscription: EventBus.SubscriptionHandle? = null
    private var exited = false
    private var initFinished = false

    private val primerViewModel: PrimerViewModel by viewModel()
    private val tokenizationViewModel: TokenizationViewModel by viewModel()

    private lateinit var sheet: CheckoutSheetFragment
    private lateinit var config: PrimerConfig

    private val viewStatusObserver = Observer<ViewStatus> {
        if (config.settings.options.showUI.not()) {
            return@Observer
        }

        val fragment = when (it) {
            ViewStatus.INITIALIZING -> InitializingFragment.newInstance()
            ViewStatus.SELECT_PAYMENT_METHOD -> SelectPaymentMethodFragment.newInstance()
            ViewStatus.VIEW_VAULTED_PAYMENT_METHODS -> VaultedPaymentMethodsFragment.newInstance()
            else -> null
        }

        if (fragment != null) {
            openFragment(
                fragment,
                initFinished
            )
        }

        if (!initFinished && it != ViewStatus.INITIALIZING) {
            initFinished = true
        }
    }

    private val checkoutEventObserver = Observer<CheckoutEvent> {
        when (it) {
            is CheckoutEvent.DismissInternal -> {
                onExit(it.data)
            }
            is CheckoutEvent.ShowSuccess -> {
                if (config.settings.options.showUI) {
                    openFragment(
                        SessionCompleteFragment.newInstance(
                            it.delay,
                            SessionCompleteViewType.Success(it.successType),
                        )
                    )
                } else {
                    onExit(CheckoutExitReason.DISMISSED_BY_USER)
                }
            }
            is CheckoutEvent.ShowError -> {
                if (config.settings.options.showUI) {
                    openFragment(
                        SessionCompleteFragment.newInstance(
                            it.delay,
                            SessionCompleteViewType.Error(it.errorType),
                        )
                    )
                } else {
                    onExit(CheckoutExitReason.DISMISSED_BY_USER)
                }
            }
            is CheckoutEvent.ToggleProgressIndicator -> {
                onToggleProgressIndicator(it.data)
            }
            is CheckoutEvent.Start3DS -> {
                startActivity(ThreeDsActivity.getLaunchIntent(this))
            }
            is CheckoutEvent.StartAsyncRedirectFlow -> {
                startActivityForResult(
                    AsyncPaymentMethodWebViewActivity.getLaunchIntent(
                        this,
                        it.redirectUrl,
                        tokenizationViewModel.asyncRedirectUrl.value.orEmpty(),
                        it.statusUrl,
                        (
                            primerViewModel.selectedPaymentMethod?.value as?
                                AsyncPaymentMethodDescriptor
                            )?.title.orEmpty(),
                        WebViewClientType.ASYNC
                    ),
                    ASYNC_METHOD_REQUEST_CODE
                )
            }
            is CheckoutEvent.StartAsyncFlow -> {
                openFragment(QrCodeFragment.newInstance(it.statusUrl), true)
            }
        }
    }

    // region Web-based payment methods
    private val webFlowPaymentDataObserver =
        Observer<BaseWebFlowPaymentData> { paymentData ->
            EventBus.broadcast(CheckoutEvent.PaymentMethodPresented)
            if (config.settings.options.preferWebView) {
                val title =
                    when (val paymentMethod = primerViewModel.selectedPaymentMethod.value) {
                        is KlarnaDescriptor -> paymentMethod.options.webViewTitle
                        is ApayaDescriptor -> paymentMethod.options.webViewTitle
                        else -> throw IllegalStateException("Unknown payment method.")
                    }

                startActivityForResult(
                    WebViewActivity.getLaunchIntent(
                        this,
                        paymentData.redirectUrl,
                        paymentData.returnUrl,
                        title.orEmpty(),
                        paymentData.getWebViewClientType()
                    ),
                    paymentData.getRequestCode()
                )
            } else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentData.redirectUrl))
                startActivity(intent)
            }
        }
    //endregion

    // region KLARNA-related observers
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
        EventBus.broadcast(CheckoutEvent.PaymentMethodPresented)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
    }

    private val payPalOrderObserver = Observer { uri: String ->
        EventBus.broadcast(CheckoutEvent.PaymentMethodPresented)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
    }
    // endregion

    private val apayaValidationObserver = Observer<Unit> {
        val paymentMethod: PaymentMethodDescriptor? =
            primerViewModel.selectedPaymentMethod.value

        if (paymentMethod !is ApayaDescriptor) return@Observer
        tokenizationViewModel.tokenize()
    }
    //endregion

    private fun emitError(error: Error) {
        val apiError = APIError(error.message ?: "")
        val event = CheckoutEvent.ApiError(apiError)
        EventBus.broadcast(event)
    }

    private fun presentFragment(behaviour: SelectedPaymentMethodBehaviour?) = when (behaviour) {
        is NewFragmentBehaviour -> openFragment(behaviour)
        is WebBrowserIntentBehaviour -> behaviour.execute(tokenizationViewModel)
        is WebViewBehaviour -> behaviour.execute(tokenizationViewModel)
        is InitialCheckRequiredBehaviour -> behaviour.execute(this, tokenizationViewModel)
        is AsyncPaymentMethodBehaviour -> behaviour.execute(tokenizationViewModel)
        // todo: refactor to sealed class or change logic altogether
        else -> Unit
    }

    private val selectedPaymentMethodObserver = Observer<PaymentMethodDescriptor?> { descriptor ->

        if (descriptor == null) return@Observer

        val type = descriptor.config.type.toString()

        val action =
            if (type == "PAYMENT_CARD") ClientSessionActionsRequest.UnsetPaymentMethod()
            else ClientSessionActionsRequest.SetPaymentMethod(type)

        primerViewModel.dispatchAction(action, false) { error: Error? ->
            runOnUiThread {
                if (error == null) presentFragment(descriptor.selectedBehaviour)
                else emitError(error)
            }
        }
    }

    private val selectedPaymentMethodBehaviourObserver =
        Observer<SelectedPaymentMethodBehaviour?> { behaviour ->
            presentFragment(behaviour)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val json = Serialization.json

        val config = intent.getStringExtra("config")
            ?.let { json.decodeFromString(PrimerConfig.serializer(), it) }
            ?: return

        if (config.settings.options.showUI.not()) {
            ensureClicksGoThrough()
        }

        this.config = config

        DIAppContext.init(applicationContext, config)

        primerViewModel.fetchConfiguration()

        sheet = CheckoutSheetFragment.newInstance()

        primerViewModel.viewStatus.observe(this, viewStatusObserver)
        primerViewModel.selectedPaymentMethod.observe(this, selectedPaymentMethodObserver)
        primerViewModel.selectedPaymentMethodBehaviour.observe(
            this,
            selectedPaymentMethodBehaviourObserver
        )
        primerViewModel.checkoutEvent.observe(this, checkoutEventObserver)

        tokenizationViewModel.getDeeplinkUrl()

        tokenizationViewModel.tokenizationCanceled.observe(this) {
            onExit(CheckoutExitReason.DISMISSED_BY_USER)
        }

        tokenizationViewModel.error.observe(this) { d ->
            if (d == null) return@observe
            val apiError = APIError(d)
            EventBus.broadcast(CheckoutEvent.ApiError(apiError))
        }

        // region KLARNA
        tokenizationViewModel.klarnaPaymentData.observe(this, webFlowPaymentDataObserver)
        tokenizationViewModel.vaultedKlarnaPayment.observe(this, klarnaVaultedObserver)
        tokenizationViewModel.klarnaError.observe(this) {
            val apiError = APIError("Failed to add Klarna payment method.")
            EventBus.broadcast(CheckoutEvent.ApiError(apiError))
        }
        // endregion

        // region apaya
        tokenizationViewModel.apayaValidationData.observe(this, apayaValidationObserver)
        tokenizationViewModel.apayaPaymentData.observe(this, webFlowPaymentDataObserver)
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
                is CheckoutEvent.StartAsyncFlow,
                is CheckoutEvent.StartAsyncRedirectFlow,
                is CheckoutEvent.Start3DS,
                is CheckoutEvent.DismissInternal,
                is CheckoutEvent.ShowSuccess,
                is CheckoutEvent.ShowError,
                is CheckoutEvent.ToggleProgressIndicator,
                -> primerViewModel.setCheckoutEvent(it)
                else -> {
                }
            }
        }

        if (!config.settings.options.showUI.not()) {
            openSheet()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription?.unregister()
        subscription = null
    }

    private fun ensureClicksGoThrough() {
        window
            .addFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
    }

    override fun onResume() {
        super.onResume()
        if (!config.settings.options.preferWebView ||
            setOf(PaymentMethodType.KLARNA, PaymentMethodType.APAYA)
                .contains(primerViewModel.selectedPaymentMethod.value?.config?.type)
                .not()
        ) {
            WebviewInteropRegister.invokeAll()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            KLARNA_REQUEST_CODE, APAYA_REQUEST_CODE ->
                handleWebFlowRequestResult(
                    resultCode,
                    data
                )
            ASYNC_METHOD_REQUEST_CODE -> handleAsyncFlowRequestResult(resultCode)
            GOOGLE_PAY_REQUEST_CODE -> handleGooglePayRequestResult(resultCode, data)
            else -> Unit
        }
    }

    private fun handleWebFlowRequestResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                val redirectUrl = data?.data.toString()
                val paymentMethod = primerViewModel.selectedPaymentMethod.value

                tokenizationViewModel.handleWebFlowRequestResult(paymentMethod, redirectUrl)
            }
            RESULT_ERROR -> {
                onExit(CheckoutExitReason.ERROR)
            }
            RESULT_CANCELED -> {
                onExit(CheckoutExitReason.DISMISSED_BY_USER)
            }
        }
    }

    private fun handleAsyncFlowRequestResult(resultCode: Int) {
        when (resultCode) {
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
        if (config.settings.options.showUI.not()) {
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

    private fun openSheet() {
        sheet.show(supportFragmentManager, sheet.tag)
    }
}
