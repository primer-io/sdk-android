package io.primer.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import io.primer.android.di.DIAppComponent
import io.primer.android.di.DIAppContext
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.Model
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.CheckoutExitInfo
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.model.json
import io.primer.android.payment.*
import io.primer.android.payment.GOCARDLESS_IDENTIFIER
import io.primer.android.payment.GOOGLE_PAY_IDENTIFIER
import io.primer.android.payment.GooglePayPaymentMethodDescriptorFactory
import io.primer.android.payment.KLARNA_IDENTIFIER
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PAYMENT_CARD_IDENTIFIER
import io.primer.android.payment.PAYPAL_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.payment.WebBrowserIntentBehaviour
import io.primer.android.payment.WebViewBehaviour
import io.primer.android.payment.google.GooglePayBridge
import io.primer.android.payment.google.GooglePayDescriptor
import io.primer.android.payment.google.GooglePayDescriptor.Companion.GOOGLE_PAY_REQUEST_CODE
import io.primer.android.payment.google.InitialCheckRequiredBehaviour
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.klarna.Klarna.Companion.KLARNA_REQUEST_CODE
import io.primer.android.payment.paypal.PayPal
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.ui.fragments.InitializingFragment
import io.primer.android.ui.fragments.ProgressIndicatorFragment
import io.primer.android.ui.fragments.SelectPaymentMethodFragment
import io.primer.android.ui.fragments.SuccessFragment
import io.primer.android.ui.fragments.VaultedPaymentMethodsFragment
import io.primer.android.viewmodel.GenericSavedStateViewModelFactory
import io.primer.android.viewmodel.GooglePayPaymentMethodChecker
import io.primer.android.viewmodel.PrimerPaymentMethodCheckerRegistry
import io.primer.android.viewmodel.PrimerPaymentMethodDescriptorResolver
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.PrimerViewModelFactory
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.ViewStatus
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
    private val mainViewModel: PrimerViewModel by viewModels {
        GenericSavedStateViewModelFactory(viewModelFactory, this)
    }

    private val model: Model by inject() // FIXME manual di here
    private val configuredPaymentMethods: List<PaymentMethod> by inject() // FIXME manual di here

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
                    behaviour.execute(
                        this,
                        tokenizationViewModel,
                        //googlePayBridge
                    )
                }
                else -> {
                    // TODO what should we do here?
                }
            }
        }
    }

    private lateinit var paymentMethodDescriptorFactory: PaymentMethodDescriptorFactory
    private lateinit var paymentMethodDescriptorResolver: PrimerPaymentMethodDescriptorResolver
    private lateinit var googlePayBridge: GooglePayBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val checkoutConfig = unmarshal<CheckoutConfig>("config")
        val paymentMethods = unmarshal<List<PaymentMethod>>("paymentMethods")

        DIAppContext.init(this, checkoutConfig, paymentMethods)

        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build()
        val paymentsClient: PaymentsClient =
            Wallet.getPaymentsClient(applicationContext, walletOptions)
        googlePayBridge = GooglePayBridge(paymentsClient)
        val googlePayChecker = GooglePayPaymentMethodChecker(googlePayBridge)

        val paymentMethodRegistrar = PrimerPaymentMethodCheckerRegistry
        paymentMethodRegistrar.register(GOOGLE_PAY_IDENTIFIER, googlePayChecker)

        paymentMethodDescriptorFactory = PaymentMethodDescriptorFactory(paymentMethodRegistrar)
        paymentMethodDescriptorFactory.register(PAYMENT_CARD_IDENTIFIER, CardPaymentMethodDescriptorFactory())
        paymentMethodDescriptorFactory.register(PAYPAL_IDENTIFIER, PayPalPaymentMethodDescriptorFactory())
        paymentMethodDescriptorFactory.register(GOCARDLESS_IDENTIFIER, GoCardlessPaymentMethodDescriptorFactory())
        paymentMethodDescriptorFactory.register(KLARNA_IDENTIFIER, KlarnaPaymentMethodDescriptorFactory())
        paymentMethodDescriptorFactory.register(GOOGLE_PAY_IDENTIFIER, GooglePayPaymentMethodDescriptorFactory(googlePayBridge))

        paymentMethodDescriptorResolver = PrimerPaymentMethodDescriptorResolver(
            localConfig = checkoutConfig,
            localPaymentMethods = configuredPaymentMethods,
            paymentMethodDescriptorFactory = paymentMethodDescriptorFactory,
            availabilityCheckers = paymentMethodRegistrar
        )
        viewModelFactory = PrimerViewModelFactory(
            model = model,
            checkoutConfig = checkoutConfig,
            primerPaymentMethodDescriptorResolver = paymentMethodDescriptorResolver
        )

        mainViewModel.initialize()

        sheet = CheckoutSheetFragment.newInstance()

        mainViewModel.viewStatus.observe(this, viewStatusObserver)
        mainViewModel.selectedPaymentMethod.observe(this, selectedPaymentMethodObserver)

        // region KLARNA
        tokenizationViewModel.klarnaPaymentData.observe(this) { (paymentUrl, redirectUrl) ->
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra(WebViewActivity.PAYMENT_URL_KEY, paymentUrl)
                putExtra(WebViewActivity.CAPTURE_URL_KEY, redirectUrl)
            }
            startActivityForResult(intent, KLARNA_REQUEST_CODE)
        }

        tokenizationViewModel.vaultedKlarnaPayment.observe(this) { data ->
            val paymentMethod: PaymentMethodDescriptor? = mainViewModel.selectedPaymentMethod.value
            val klarna = paymentMethod as? Klarna
                ?: return@observe // if we are getting an emission here it means we're currently dealing with klarna

            klarna.setTokenizableValue(
                "klarnaAuthorizationToken",
                data.optString("klarnaAuthorizationToken")
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
            GOOGLE_PAY_REQUEST_CODE -> handleGooglePayRequestResult(resultCode, data)
            else -> {
                // TODO error: unexpected request code
            }
        }
    }

    private fun handleKlarnaRequestResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                val redirectUrl = data?.extras?.getString(WebViewActivity.REDIRECT_URL_KEY)
                val paymentMethod = mainViewModel.selectedPaymentMethod.value
                val klarna = paymentMethod as? Klarna

                tokenizationViewModel.handleKlarnaRequestResult(redirectUrl, klarna)
            }
            RESULT_CANCELED -> {
                onExit(CheckoutExitReason.DISMISSED_BY_USER)
            }
        }
    }

    private fun handleGooglePayRequestResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                val paymentMethod = mainViewModel.selectedPaymentMethod.value
                val googlePay = paymentMethod as? GooglePayDescriptor
                data?.let {
                    val paymentData = PaymentData.getFromIntent(data)
                    tokenizationViewModel.handleGooglePayRequestResult(paymentData, googlePay)
                }
            }
            RESULT_CANCELED -> {
                // onExit(CheckoutExitReason.DISMISSED_BY_USER)
                // TODO ?
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
