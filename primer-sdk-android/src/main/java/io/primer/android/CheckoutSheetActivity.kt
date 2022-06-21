package io.primer.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.wallet.PaymentData
import io.primer.android.analytics.data.models.TimerId
import io.primer.android.analytics.data.models.TimerType
import io.primer.android.analytics.domain.models.TimerAnalyticsParams
import io.primer.android.components.domain.inputs.models.valueBy
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.di.DIAppComponent
import io.primer.android.di.DIAppContext
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.CheckoutExitInfo
import io.primer.android.model.CheckoutExitReason
import io.primer.android.model.Serialization
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
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.android.ui.base.webview.BaseWebFlowPaymentData
import io.primer.android.ui.base.webview.WebViewActivity
import io.primer.android.ui.base.webview.WebViewActivity.Companion.RESULT_ERROR
import io.primer.android.ui.base.webview.WebViewClientType
import io.primer.android.ui.extensions.popBackStackToRoot
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.ui.fragments.InitializingFragment
import io.primer.android.ui.fragments.PaymentMethodStatusFragment
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
import org.koin.core.component.inject

@KoinApiExtension
internal class CheckoutSheetActivity : AppCompatActivity(), DIAppComponent {

    private var subscription: EventBus.SubscriptionHandle? = null
    private var exited = false
    private var initFinished = false

    private val primerViewModel: PrimerViewModel by viewModel()
    private val tokenizationViewModel: TokenizationViewModel by viewModel()
    private val errorEventResolver: BaseErrorEventResolver by inject()

    private lateinit var sheet: CheckoutSheetFragment
    private lateinit var config: PrimerConfig

    private val viewStatusObserver = Observer<ViewStatus> { viewStatus ->
        if (config.settings.fromHUC) {
            return@Observer
        }

        val fragment = when (viewStatus) {
            ViewStatus.INITIALIZING -> {
                InitializingFragment.newInstance()
            }
            ViewStatus.SELECT_PAYMENT_METHOD -> {
                SelectPaymentMethodFragment.newInstance()
            }
            ViewStatus.VIEW_VAULTED_PAYMENT_METHODS -> {
                VaultedPaymentMethodsFragment.newInstance()
            }
        }

        when {
            viewStatus == ViewStatus.INITIALIZING &&
                config.settings.uiOptions.isInitScreenEnabled.not() -> sheet?.dialog?.hide()
            else -> sheet?.dialog?.show()
        }

        openFragment(
            fragment,
            initFinished
        )

        if (!initFinished && viewStatus != ViewStatus.INITIALIZING) {
            initFinished = true
        }
    }

    private val checkoutEventObserver = Observer<CheckoutEvent> {
        when (it) {
            is CheckoutEvent.DismissInternal -> {
                onExit(it.data)
            }
            is CheckoutEvent.ShowSuccess -> {
                if (config.settings.uiOptions.isSuccessScreenEnabled) {
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
                if (config.settings.uiOptions.isErrorScreenEnabled) {
                    openFragment(
                        SessionCompleteFragment.newInstance(
                            it.delay,
                            SessionCompleteViewType.Error(it.errorType, it.message),
                        )
                    )
                } else {
                    onExit(CheckoutExitReason.DISMISSED_BY_USER)
                }
            }
            is CheckoutEvent.Start3DS -> {
                it.processor3DSData?.let { data ->
                    startActivityForResult(
                        AsyncPaymentMethodWebViewActivity.getLaunchIntent(
                            this,
                            data.redirectUrl,
                            "",
                            data.statusUrl,
                            data.title,
                            PaymentMethodType.PAYMENT_CARD,
                            WebViewClientType.ASYNC
                        ),
                        ASYNC_METHOD_REQUEST_CODE
                    )
                } ?: run {
                    startActivity(ThreeDsActivity.getLaunchIntent(this))
                }
            }
            is CheckoutEvent.StartAsyncRedirectFlow -> {
                startActivityForResult(
                    AsyncPaymentMethodWebViewActivity.getLaunchIntent(
                        this,
                        it.redirectUrl,
                        tokenizationViewModel.asyncRedirectUrl.value.orEmpty(),
                        it.statusUrl,
                        (
                            primerViewModel.selectedPaymentMethod.value as?
                                AsyncPaymentMethodDescriptor
                            )?.title.orEmpty(),
                        it.paymentMethodType,
                        WebViewClientType.ASYNC
                    ),
                    ASYNC_METHOD_REQUEST_CODE
                )
            }
            is CheckoutEvent.StartAsyncFlow -> {
                when (it.clientTokenIntent) {
                    ClientTokenIntent.ADYEN_BLIK_REDIRECTION -> {
                        sheet.popBackStackToRoot()
                        openFragment(
                            PaymentMethodStatusFragment.newInstance(
                                it.statusUrl,
                                it.paymentMethodType
                            ),
                            true
                        )
                    }
                    ClientTokenIntent.XFERS_PAYNOW_REDIRECTION -> openFragment(
                        QrCodeFragment.newInstance(
                            it.statusUrl,
                            it.paymentMethodType
                        ),
                        true
                    )
                    else -> Unit
                }
            }
        }
    }

    // region Web-based payment methods
    private val webFlowPaymentDataObserver =
        Observer<BaseWebFlowPaymentData> { paymentData ->
            EventBus.broadcast(CheckoutEvent.PaymentMethodPresented)
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
            data.valueBy("customerTokenId")
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
        errorEventResolver.resolve(error, ErrorMapperType.DEFAULT)
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

    private val actionNavigateObserver = Observer<SelectedPaymentMethodBehaviour> { behaviour ->
        behaviour ?: return@Observer
        presentFragment(behaviour)
    }

    private val selectedPaymentMethodObserver = Observer<PaymentMethodDescriptor?> { descriptor ->
        if (descriptor == null) return@Observer

        val type = descriptor.config.type

        val actionParams =
            if (type == PaymentMethodType.PAYMENT_CARD) ActionUpdateUnselectPaymentMethodParams
            else ActionUpdateSelectPaymentMethodParams(type)

        primerViewModel.dispatchAction(actionParams, false) { error: Error? ->
            runOnUiThread {
                if (error == null) {
                    presentFragment(descriptor.selectedBehaviour)
                    primerViewModel.setState(SessionState.AWAITING_USER)
                } else emitError(error)
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

        if (config.settings.fromHUC) {
            ensureClicksGoThrough()
        }

        this.config = config

        DIAppContext.init(applicationContext, config)

        primerViewModel.initializeAnalytics()

        addTimerDurationEvent(TimerType.START)

        primerViewModel.fetchConfiguration()

        sheet = CheckoutSheetFragment.newInstance()
        primerViewModel.viewStatus.observe(this, viewStatusObserver)
        primerViewModel.selectedPaymentMethod.observe(this, selectedPaymentMethodObserver)
        primerViewModel.selectedPaymentMethodBehaviour.observe(
            this,
            selectedPaymentMethodBehaviourObserver
        )
        primerViewModel.checkoutEvent.observe(this, checkoutEventObserver)
        primerViewModel.navigateActionEvent.observe(this, actionNavigateObserver)

        tokenizationViewModel.getDeeplinkUrl()

        tokenizationViewModel.tokenizationCanceled.observe(this) {
            errorEventResolver.resolve(
                PaymentMethodCancelledException(it),
                ErrorMapperType.DEFAULT
            )
            onExit(CheckoutExitReason.DISMISSED_BY_USER)
        }

        tokenizationViewModel.error.observe(this) { error ->
            errorEventResolver.resolve(
                error,
                ErrorMapperType.SESSION_CREATE
            )
        }

        // region KLARNA
        tokenizationViewModel.klarnaPaymentData.observe(this, webFlowPaymentDataObserver)
        tokenizationViewModel.vaultedKlarnaPayment.observe(this, klarnaVaultedObserver)
        tokenizationViewModel.klarnaError.observe(this) {
            errorEventResolver.resolve(
                it,
                ErrorMapperType.SESSION_CREATE
            )
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
                -> primerViewModel.setCheckoutEvent(it)
                else -> {
                }
            }
        }

        if (config.settings.fromHUC.not()) {
            openSheet()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearSubscription()
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
        // todo
        if (
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
                errorEventResolver.resolve(
                    PaymentMethodCancelledException(
                        PaymentMethodType.safeValueOf(
                            primerViewModel.selectedPaymentMethod.value?.config?.type?.name
                        )
                    ),
                    ErrorMapperType.DEFAULT
                )
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
                errorEventResolver.resolve(
                    PaymentMethodCancelledException(PaymentMethodType.GOOGLE_PAY),
                    ErrorMapperType.DEFAULT
                )
                onExit(CheckoutExitReason.DISMISSED_BY_USER)
            }
        }
    }

    private fun onExit(reason: CheckoutExitReason) {
        if (!exited) {
            addTimerDurationEvent(TimerType.END)
            exited = true
            EventBus.broadcast(
                CheckoutEvent.Exit(CheckoutExitInfo(reason))
            )
            clearSubscription()
            finish()
        }
    }

    private fun openFragment(fragment: Fragment, returnToPreviousOnBack: Boolean = false) {
        openFragment(NewFragmentBehaviour({ fragment }, returnToPreviousOnBack))
    }

    private fun openFragment(behaviour: NewFragmentBehaviour) {
        if (config.settings.fromHUC) {
            return
        }

        behaviour.execute(sheet)
    }

    private fun openSheet() {
        sheet.show(supportFragmentManager, sheet.tag)
    }

    private fun addTimerDurationEvent(type: TimerType) = primerViewModel.addAnalyticsEvent(
        TimerAnalyticsParams(
            TimerId.CHECKOUT_DURATION,
            type
        )
    )

    private fun clearSubscription() {
        subscription?.unregister()
        subscription = null
    }
}
