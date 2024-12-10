package io.primer.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import io.primer.android.analytics.data.models.TimerId
import io.primer.android.analytics.data.models.TimerType
import io.primer.android.analytics.domain.models.TimerAnalyticsParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.components.di.DISdkContextInitializer
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.extensions.inject
import io.primer.android.core.di.extensions.viewModel
import io.primer.android.core.extensions.getParcelableCompat
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.di.CheckoutConfigContainer
import io.primer.android.di.CountriesDataStorageContainer
import io.primer.android.di.FormsContainer
import io.primer.android.payment.NativeUiSelectedPaymentMethodManagerBehaviour
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.helpers.CheckoutExitHandler
import io.primer.android.qrcode.QrCodeCheckoutAdditionalInfo
import io.primer.android.stripe.ach.api.additionalInfo.AchAdditionalInfo
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.InitializingFragment
import io.primer.android.ui.fragments.SelectPaymentMethodFragment
import io.primer.android.ui.fragments.SessionCompleteFragment
import io.primer.android.ui.fragments.SessionCompleteViewType
import io.primer.android.ui.fragments.VaultedPaymentMethodsCvvRecaptureFragment
import io.primer.android.ui.fragments.VaultedPaymentMethodsFragment
import io.primer.android.ui.fragments.forms.QrCodeFragment
import io.primer.android.ui.fragments.stripe.ach.StripeAchMandateFragment
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.PrimerViewModelFactory
import io.primer.android.viewmodel.ViewStatus
import io.primer.paymentMethodCoreUi.core.ui.BaseCheckoutActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
internal class CheckoutSheetActivity : BaseCheckoutActivity(), AchMandateActionHandler {

    private var exited = false
    private var initFinished = false

    private val primerViewModel: PrimerViewModel
        by viewModel<PrimerViewModel, PrimerViewModelFactory>()

    private val checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler by inject()
    private val checkoutExitHandler: CheckoutExitHandler by inject()
    private val config: PrimerConfig by inject()

    private lateinit var sheet: CheckoutSheetFragment

    private var displayMandateAchAdditionalInfo: AchAdditionalInfo.DisplayMandate? = null

    private val viewStatusObserver = Observer<ViewStatus> { viewStatus ->
        val fragment = when {
            viewStatus is ViewStatus.Initializing &&
                config.settings.uiOptions.isInitScreenEnabled -> {
                InitializingFragment.newInstance()
            }

            viewStatus is ViewStatus.SelectPaymentMethod -> {
                SelectPaymentMethodFragment.newInstance()
            }

            viewStatus is ViewStatus.ViewVaultedPaymentMethods -> {
                VaultedPaymentMethodsFragment.newInstance()
            }

            viewStatus is ViewStatus.VaultedPaymentRecaptureCvv -> {
                VaultedPaymentMethodsCvvRecaptureFragment.newInstance()
            }

            viewStatus is ViewStatus.ShowError ->
                SessionCompleteFragment.newInstance(
                    delay = viewStatus.delay,
                    viewType = SessionCompleteViewType.Error(viewStatus.errorType, viewStatus.message)
                )

            viewStatus is ViewStatus.ShowSuccess -> {
                val behaviour = (
                    primerViewModel.selectedPaymentMethod.value
                        ?: primerViewModel.selectedSavedPaymentMethodDescriptor
                    )?.createSuccessBehavior(viewStatus)
                if (behaviour != null) {
                    openFragment(behaviour)
                    return@Observer
                } else {
                    SessionCompleteFragment.newInstance(
                        delay = viewStatus.delay,
                        viewType = SessionCompleteViewType.Error(ErrorType.DEFAULT, getString(R.string.error_default))
                    )
                }
            }

            viewStatus is ViewStatus.PollingStarted -> {
                val behaviour = primerViewModel.selectedPaymentMethod.value?.createPollingStartedBehavior(viewStatus)
                if (behaviour != null) {
                    openFragment(behaviour)
                    return@Observer
                } else {
                    null
                }
            }

            viewStatus is ViewStatus.Dismiss -> {
                onExit()
                null
            }

            else -> null
        }

        if (!initFinished && viewStatus != ViewStatus.Initializing) {
            initFinished = true
        }

        fragment?.let {
            openFragment(
                fragment = it,
                returnToPreviousOnBack = initFinished,
                tag = if (it is SelectPaymentMethodFragment) {
                    SelectPaymentMethodFragment.TAG
                } else {
                    null
                }
            )
        } ?: run {
            sheet.dialog?.hide()
        }
    }

    private fun presentFragment(behaviour: PaymentMethodBehaviour?) = when (behaviour) {
        is NewFragmentBehaviour -> openFragment(behaviour)
        is NativeUiSelectedPaymentMethodManagerBehaviour -> {
            val nativeUiManager = behaviour.execute(this)
            primerViewModel.setSelectedPaymentMethodNativeUiManager(nativeUiManager)
        }
        // todo: refactor to sealed class or change logic altogether
        else -> Unit
    }

    private val actionNavigateObserver = Observer<PaymentMethodBehaviour> { behaviour ->
        presentFragment(behaviour)
    }

    private val selectedPaymentMethodObserver = Observer<PaymentMethodDropInDescriptor?> { descriptor ->
        if (descriptor == null) return@Observer

        val type = descriptor.paymentMethodType

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
                    // no-op error was emitted
                }
            }
        }
    }

    private val paymentMethodBehaviourObserver =
        Observer<PaymentMethodBehaviour?> { behaviour ->
            presentFragment(behaviour)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isFinishing) {
            return
        }

        logReporter.debug(
            "Creating CheckoutSheetActivity (hashcode ${hashCode()}); " +
                "Drop-in container: ${DISdkContext.dropInSdkContainer}"
        )

        intent.getParcelableCompat<PrimerConfig>(PRIMER_CONFIG_KEY)?.let { config ->
            if (DISdkContext.dropInSdkContainer == null) {
                DISdkContextInitializer.initDropIn(
                    config = config,
                    context = applicationContext
                )
                DISdkContext.dropInSdkContainer?.apply {
                    registerContainer(CheckoutConfigContainer { getSdkContainer() })
                    registerContainer(CountriesDataStorageContainer { getSdkContainer() })
                    registerContainer(FormsContainer { getSdkContainer() })
                }
            }
            intent.removeExtra(PRIMER_CONFIG_KEY)
        } ?: run {
            logReporter.warn(
                "Finishing CheckoutSheetActivity " +
                    "(hashcode ${hashCode()}) because Primer config is missing"
            )
            finish()
            return
        }

        addTimerDurationEvent(TimerType.START)

        primerViewModel.fetchConfiguration()

        sheet = CheckoutSheetFragment.newInstance()
        primerViewModel.viewStatus.observe(this, viewStatusObserver)
        primerViewModel.selectedPaymentMethod.observe(this, selectedPaymentMethodObserver)
        primerViewModel.paymentMethodBehaviour.observe(
            this,
            paymentMethodBehaviourObserver
        )
        primerViewModel.navigateActionEvent.observe(this, actionNavigateObserver)

        lifecycleScope.launch { collectCheckoutAdditionalInfo() }

        openSheet()
    }

    private suspend fun collectCheckoutAdditionalInfo() {
        checkoutAdditionalInfoHandler.checkoutAdditionalInfo.flowWithLifecycle(lifecycle).collectLatest {
            when (it) {
                is QrCodeCheckoutAdditionalInfo -> {
                    openFragment(
                        QrCodeFragment.newInstance(
                            qrCodeBase64 = it.qrCodeBase64,
                            qrCodeUrl = it.qrCodeUrl,
                            statusUrl = it.statusUrl,
                            paymentMethodType = it.paymentMethodType
                        ),
                        true
                    )
                }

                is AchAdditionalInfo.DisplayMandate -> {
                    displayMandateAchAdditionalInfo = it
                    sheet.childFragmentManager.commit {
                        replace(R.id.checkout_sheet_content, StripeAchMandateFragment.newInstance())
                    }
                }
            }
        }
    }

    override suspend fun handleAchMandateAction(isAccepted: Boolean) {
        displayMandateAchAdditionalInfo?.let {
            displayMandateAchAdditionalInfo = null
            if (isAccepted) {
                it.onAcceptMandate()
            } else {
                it.onDeclineMandate()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val fragments = sheet.childFragmentManager.fragments
        val descriptor = primerViewModel.selectedPaymentMethod.value
        if (fragments.isEmpty() && descriptor != null) {
            sheet.dialog?.hide()
        }
    }

    private fun onExit() {
        logReporter.debug("Exiting CheckoutSheetActivity (hashcode: ${hashCode()})")
        if (!exited) {
            addTimerDurationEvent(TimerType.END)
            exited = true
            checkoutExitHandler.handle()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logReporter.debug("Destroying CheckoutSheetActivity (hashcode: ${hashCode()})")
        DISdkContextInitializer.clearDropIn()
    }

    private fun openFragment(fragment: Fragment, returnToPreviousOnBack: Boolean = false, tag: String? = null) {
        sheet.dialog?.show()
        openFragment(NewFragmentBehaviour({ fragment }, returnToPreviousOnBack, tag))
    }

    private fun openFragment(behaviour: NewFragmentBehaviour) {
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

    internal companion object {

        const val PRIMER_CONFIG_KEY = "PRIMER_CONFIG"
    }
}
