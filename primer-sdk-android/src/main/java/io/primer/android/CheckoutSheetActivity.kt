package io.primer.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import io.primer.android.analytics.data.models.TimerId
import io.primer.android.analytics.data.models.TimerType
import io.primer.android.analytics.domain.models.TimerAnalyticsParams
import io.primer.android.components.presentation.paymentMethods.nativeUi.webRedirect.AsyncState
import io.primer.android.components.ui.activity.HeadlessActivity
import io.primer.android.components.ui.activity.PaymentMethodLauncherParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.di.DISdkContext
import io.primer.android.di.extension.inject
import io.primer.android.di.extension.viewModel
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.extensions.getParcelable
import io.primer.android.model.CheckoutExitInfo
import io.primer.android.model.CheckoutExitReason
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.SelectedPaymentMethodManagerBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor.Companion.ASYNC_METHOD_REQUEST_CODE
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.android.ui.base.webview.WebViewClientType
import io.primer.android.ui.extensions.popBackStackToRoot
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.ui.fragments.InitializingFragment
import io.primer.android.ui.fragments.PaymentMethodStatusFragment
import io.primer.android.ui.fragments.SelectPaymentMethodFragment
import io.primer.android.ui.fragments.SessionCompleteFragment
import io.primer.android.ui.fragments.SessionCompleteViewType
import io.primer.android.ui.fragments.VaultedPaymentMethodsCvvRecaptureFragment
import io.primer.android.ui.fragments.VaultedPaymentMethodsFragment
import io.primer.android.ui.fragments.forms.FastBankTransferFragment
import io.primer.android.ui.fragments.forms.PromptPayFragment
import io.primer.android.ui.fragments.forms.QrCodeFragment
import io.primer.android.ui.fragments.multibanko.MultibancoPaymentFragment
import io.primer.android.ui.mock.PaymentMethodMockActivity
import io.primer.android.ui.payment.processor3ds.Processor3dsWebViewActivity
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.PrimerViewModelFactory
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.TokenizationViewModelFactory
import io.primer.android.viewmodel.ViewStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal class CheckoutSheetActivity : BaseCheckoutActivity() {

    private var subscription: EventBus.SubscriptionHandle? = null
    private var exited = false
    private var initFinished = false

    private val primerViewModel: PrimerViewModel
        by viewModel<PrimerViewModel, PrimerViewModelFactory>()
    private val tokenizationViewModel: TokenizationViewModel
        by viewModel<TokenizationViewModel, TokenizationViewModelFactory>()

    private val errorEventResolver: BaseErrorEventResolver by inject()
    private val config: PrimerConfig by inject()

    private lateinit var sheet: CheckoutSheetFragment

    private val viewStatusObserver = Observer<ViewStatus> { viewStatus ->
        val fragment = when {
            viewStatus == ViewStatus.INITIALIZING &&
                config.settings.uiOptions.isInitScreenEnabled -> {
                InitializingFragment.newInstance()
            }

            viewStatus == ViewStatus.SELECT_PAYMENT_METHOD -> {
                SelectPaymentMethodFragment.newInstance()
            }

            viewStatus == ViewStatus.VIEW_VAULTED_PAYMENT_METHODS -> {
                VaultedPaymentMethodsFragment.newInstance()
            }

            viewStatus == ViewStatus.VAULTED_PAYMENT_RECAPTURE_CVV -> {
                VaultedPaymentMethodsCvvRecaptureFragment.newInstance()
            }

            else -> null
        }

        fragment?.let {
            openFragment(
                it,
                initFinished
            )
        } ?: run {
            sheet?.dialog?.hide()
        }

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
                            SessionCompleteViewType.Success(it.successType)
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
                            SessionCompleteViewType.Error(it.errorType, it.message)
                        )
                    )
                } else {
                    onExit(CheckoutExitReason.DISMISSED_BY_USER)
                }
            }

            is CheckoutEvent.Start3DSMock -> {
                startActivity(
                    PaymentMethodMockActivity.getLaunchIntent(
                        this,
                        PaymentMethodType.PAYMENT_CARD.name
                    )
                )
            }

            is CheckoutEvent.Start3DS -> {
                it.processor3DSData?.let { data ->
                    startActivityForResult(
                        Processor3dsWebViewActivity.getLaunchIntent(
                            this,
                            data.redirectUrl,
                            "",
                            data.statusUrl,
                            data.title,
                            PaymentMethodType.PAYMENT_CARD.name,
                            WebViewClientType.PROCESSOR_3DS
                        ),
                        ASYNC_METHOD_REQUEST_CODE
                    )
                } ?: run {
                    startActivity(ThreeDsActivity.getLaunchIntent(this))
                }
            }

            is CheckoutEvent.StartAsyncRedirectFlow -> {
                // this will be removed when Drop-In starts using Raw Data Managers
                startActivity(
                    HeadlessActivity.getLaunchIntent(
                        this,
                        PaymentMethodLauncherParams(
                            it.paymentMethodType,
                            it.sessionIntent,
                            AsyncState.StartRedirect(
                                it.title,
                                it.paymentMethodType,
                                it.redirectUrl,
                                it.statusUrl,
                                it.deeplinkUrl
                            )
                        )
                    )
                )
            }

            is CheckoutEvent.StartAsyncFlow -> {
                when (it.clientTokenIntent) {
                    ClientTokenIntent.ADYEN_BLIK_REDIRECTION.name -> {
                        sheet.popBackStackToRoot()
                        openFragment(
                            PaymentMethodStatusFragment.newInstance(
                                it.statusUrl,
                                it.paymentMethodType
                            ),
                            true
                        )
                    }

                    ClientTokenIntent.XFERS_PAYNOW_REDIRECTION.name -> openFragment(
                        QrCodeFragment.newInstance(
                            it.statusUrl,
                            it.paymentMethodType
                        ),
                        true
                    )

                    ClientTokenIntent.RAPYD_FAST_REDIRECTION.name -> openFragment(
                        FastBankTransferFragment.newInstance(
                            it.statusUrl,
                            it.paymentMethodType
                        ),
                        true
                    )

                    ClientTokenIntent.OMISE_PROMPTPAY_REDIRECTION.name,
                    ClientTokenIntent.RAPYD_PROMPTPAY_REDIRECTION.name -> openFragment(
                        PromptPayFragment.newInstance(
                            it.statusUrl,
                            it.paymentMethodType
                        ),
                        true
                    )
                }
            }

            is CheckoutEvent.StartVoucherFlow -> {
                when (it.paymentMethodType) {
                    PaymentMethodType.ADYEN_MULTIBANCO.name -> {
                        openFragment(
                            MultibancoPaymentFragment.newInstance(
                                it.statusUrl,
                                it.paymentMethodType
                            ),
                            true
                        )
                    }
                }
            }

            else -> Unit
        }
    }

    private fun emitError(error: Error) {
        errorEventResolver.resolve(error, ErrorMapperType.DEFAULT)
    }

    private fun presentFragment(behaviour: SelectedPaymentMethodBehaviour?) = when (behaviour) {
        is NewFragmentBehaviour -> openFragment(behaviour)
        is SelectedPaymentMethodManagerBehaviour -> behaviour.execute(this, primerViewModel)
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
            if (type == PaymentMethodType.PAYMENT_CARD.name) {
                ActionUpdateUnselectPaymentMethodParams
            } else {
                ActionUpdateSelectPaymentMethodParams(type)
            }

        primerViewModel.dispatchAction(actionParams, false) { error: Error? ->
            runOnUiThread {
                if (error == null) {
                    presentFragment(descriptor.selectedBehaviour)
                    primerViewModel.setState(SessionState.AWAITING_USER)
                } else {
                    emitError(error)
                }
            }
        }
    }

    private val selectedPaymentMethodBehaviourObserver =
        Observer<SelectedPaymentMethodBehaviour?> { behaviour ->
            presentFragment(behaviour)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        intent.getParcelable<PrimerConfig>(PRIMER_CONFIG_KEY)?.let { config ->
            DISdkContext.init(
                config,
                applicationContext
            )
            intent.removeExtra(PRIMER_CONFIG_KEY)
        } ?: run { finish() }

        super.onCreate(savedInstanceState)

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

        subscription = EventBus.subscribe {
            when (it) {
                is CheckoutEvent.StartAsyncFlow,
                is CheckoutEvent.StartVoucherFlow,
                is CheckoutEvent.StartAsyncRedirectFlow,
                is CheckoutEvent.Start3DS,
                is CheckoutEvent.Start3DSMock,
                is CheckoutEvent.DismissInternal,
                is CheckoutEvent.ShowSuccess,
                is CheckoutEvent.ShowError
                -> primerViewModel.setCheckoutEvent(it)

                else -> Unit
            }
        }

        if (config.settings.fromHUC.not()) {
            openSheet()
        }
    }

    override fun onStart() {
        super.onStart()
        if (config.settings.fromHUC.not()) {
            val fragments = sheet?.childFragmentManager?.fragments
            val descriptor = primerViewModel.selectedPaymentMethod.value
            if (fragments.isNullOrEmpty() && descriptor != null) {
                sheet?.dialog?.hide()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearSubscription()
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
        sheet?.dialog?.show()
        openFragment(NewFragmentBehaviour({ fragment }, returnToPreviousOnBack))
    }

    private fun openFragment(behaviour: NewFragmentBehaviour) {
        if (config.settings.fromHUC) {
            return
        }

        behaviour.execute(sheet)
        sheet.dialog?.show()
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

    internal companion object {

        const val PRIMER_CONFIG_KEY = "PRIMER_CONFIG"
    }
}
